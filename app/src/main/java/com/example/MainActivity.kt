package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.GhorKhataDatabase
import com.example.data.model.CalendarDayEntry
import com.example.data.model.EntryType
import com.example.data.repository.GhorKhataRepository
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.i18n.Strings
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GhorKhataViewModel
import com.example.ui.viewmodel.GhorKhataViewModelFactory

enum class AppScreen {
    HOME,
    CALENDAR,
    MILK,
    BILLS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = GhorKhataDatabase.getDatabase(this, lifecycleScope)
        val repository = GhorKhataRepository(database.ghorKhataDao())

        setContent {
            MyApplicationTheme {
                val viewModel: GhorKhataViewModel = viewModel(
                    factory = GhorKhataViewModelFactory(repository)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val isBn = uiState.language != "en"

                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var showQuickAddSheet by remember { mutableStateOf(false) }
                var quickAddInitialType by remember { mutableStateOf(EntryType.REFILL) }
                var quickAddInitialDate by remember { mutableStateOf<String?>(null) }

                // Back navigation: if not on Home, return to Home on back press
                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HOME,
                                onClick = { currentScreen = AppScreen.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = Strings.get("nav_home", isBn)) },
                                label = { Text(Strings.get("nav_home", isBn)) },
                                modifier = Modifier.testTag("nav_item_home")
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CALENDAR,
                                onClick = { currentScreen = AppScreen.CALENDAR },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = Strings.get("nav_calendar", isBn)) },
                                label = { Text(Strings.get("nav_calendar", isBn)) },
                                modifier = Modifier.testTag("nav_item_calendar")
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.MILK,
                                onClick = { currentScreen = AppScreen.MILK },
                                icon = { Icon(Icons.Default.LocalDrink, contentDescription = Strings.get("nav_milk", isBn)) },
                                label = { Text(Strings.get("nav_milk", isBn)) },
                                modifier = Modifier.testTag("nav_item_milk")
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.BILLS,
                                onClick = { currentScreen = AppScreen.BILLS },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = Strings.get("nav_bills", isBn)) },
                                label = { Text(Strings.get("nav_bills", isBn)) },
                                modifier = Modifier.testTag("nav_item_bills")
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SETTINGS,
                                onClick = { currentScreen = AppScreen.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = Strings.get("nav_settings", isBn)) },
                                label = { Text(Strings.get("nav_settings", isBn)) },
                                modifier = Modifier.testTag("nav_item_settings")
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                quickAddInitialType = when (currentScreen) {
                                    AppScreen.MILK -> EntryType.MILK_PICKUP
                                    AppScreen.BILLS -> EntryType.BILL_PAYMENT
                                    else -> EntryType.REFILL
                                }
                                quickAddInitialDate = uiState.selectedCalendarDate
                                showQuickAddSheet = true
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_quick_add")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = Strings.get("quick_add_title", isBn))
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(
                                uiState = uiState,
                                isBn = isBn,
                                onQuickAdd = { type ->
                                    quickAddInitialType = type
                                    quickAddInitialDate = null
                                    showQuickAddSheet = true
                                },
                                onNavigateToMilk = { currentScreen = AppScreen.MILK },
                                onNavigateToBills = { currentScreen = AppScreen.BILLS },
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                onToggleLanguage = {
                                    viewModel.updateLanguage(if (isBn) "en" else "bn")
                                },
                                onMarkBillPaidQuick = { billId, month, amount ->
                                    viewModel.markBillPaid(billId, month, amount, uiState.selectedCalendarDate)
                                }
                            )

                            AppScreen.CALENDAR -> CalendarScreen(
                                uiState = uiState,
                                isBn = isBn,
                                onSelectDate = { date ->
                                    viewModel.selectCalendarDate(date)
                                },
                                onQuickAddForDate = { date ->
                                    quickAddInitialDate = date
                                    quickAddInitialType = EntryType.REFILL
                                    showQuickAddSheet = true
                                },
                                onDeleteEntry = { entry ->
                                    when (entry.type) {
                                        EntryType.REFILL -> viewModel.deletePurchase(entry.id)
                                        EntryType.MILK_PICKUP -> viewModel.deleteMilkPickup(entry.id)
                                        EntryType.MILK_PAYMENT -> viewModel.deleteMilkPayment(entry.id)
                                        EntryType.MILK_SKIP -> viewModel.deleteMilkSkip(entry.id)
                                        EntryType.BILL_PAYMENT -> {
                                            // Find payment id for bill
                                            val pay = uiState.billStatuses.find { it.bill.id == entry.id }?.payment
                                            if (pay != null) viewModel.deleteBillPayment(pay.id)
                                        }
                                    }
                                }
                            )

                            AppScreen.MILK -> MilkScreen(
                                uiState = uiState,
                                isBn = isBn,
                                onQuickAdd = { type ->
                                    quickAddInitialType = type
                                    showQuickAddSheet = true
                                },
                                onSelectMonth = { month ->
                                    viewModel.selectMonth(month)
                                },
                                onDeletePickup = { viewModel.deleteMilkPickup(it) },
                                onDeletePayment = { viewModel.deleteMilkPayment(it) },
                                onDeleteSkip = { viewModel.deleteMilkSkip(it) }
                            )

                            AppScreen.BILLS -> BillsScreen(
                                uiState = uiState,
                                isBn = isBn,
                                onSelectMonth = { month ->
                                    viewModel.selectMonth(month)
                                },
                                onMarkPaid = { billId, month, amount, date ->
                                    viewModel.markBillPaid(billId, month, amount, date)
                                },
                                onDeletePayment = { viewModel.deleteBillPayment(it) },
                                onToggleActive = { viewModel.toggleBillActive(it) },
                                onAddNewBill = { name, nameBn, amount, dueDay ->
                                    viewModel.addBill(name, nameBn, amount, dueDay)
                                }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                uiState = uiState,
                                isBn = isBn,
                                onToggleLanguage = { viewModel.updateLanguage(it) },
                                onToggleBengaliNumerals = { viewModel.updateBengaliNumerals(it) },
                                onUpdateMilkPrice = { viewModel.updateMilkPrice(it) },
                                onSetActiveUser = { viewModel.setActiveUser(it) },
                                onAddRefillItem = { name, nameBn, category, unit, price ->
                                    viewModel.addRefillItem(name, nameBn, category, unit, price)
                                },
                                onJoinHousehold = { viewModel.joinHousehold(it) },
                                onCreateHousehold = { name, code, members ->
                                    viewModel.updateHousehold(name, code, members)
                                },
                                onClearAllData = { viewModel.clearAllActivity() }
                            )
                        }

                        // Quick Add Modal Bottom Sheet
                        if (showQuickAddSheet) {
                            QuickAddBottomSheet(
                                initialType = quickAddInitialType,
                                initialDate = quickAddInitialDate,
                                isBn = isBn,
                                refillItems = uiState.refillItems,
                                bills = uiState.bills,
                                suggestedMilkBottles = uiState.suggestedMilkPickupBottles,
                                activeUser = uiState.activeUser,
                                onDismiss = { showQuickAddSheet = false },
                                onSaveRefill = { itemId, date, qty, price, note ->
                                    viewModel.logPurchase(itemId, date, qty, price, note)
                                },
                                onSaveMilkPickup = { date, bottles ->
                                    viewModel.logMilkPickup(date, bottles)
                                },
                                onSaveMilkPayment = { date, amount, note ->
                                    viewModel.logMilkPayment(date, amount, note)
                                },
                                onSaveBillPayment = { billId, month, amount, date ->
                                    viewModel.markBillPaid(billId, month, amount, date)
                                },
                                onSaveMilkSkip = { date, note ->
                                    viewModel.logMilkSkip(date, note)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
