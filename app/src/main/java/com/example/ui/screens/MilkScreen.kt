package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryType
import com.example.data.model.MilkPickup
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.GhorKhataUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MilkScreen(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onQuickAdd: (EntryType) -> Unit,
    onSelectMonth: (String) -> Unit,
    onDeletePickup: (Long) -> Unit,
    onDeletePayment: (Long) -> Unit,
    onDeleteSkip: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pickups & Skips, 1: Payments

    val summary = uiState.milkSummary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("milk_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Bar
        item {
            MonthSelectorCard(
                selectedMonth = uiState.selectedMonth,
                isBn = isBn,
                onMonthChanged = onSelectMonth
            )
        }

        // Monthly Financial Card with Carry-Over
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("milk_summary_title", isBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${Strings.get("bottle_price", isBn)}: ${Strings.formatCurrency(summary.pricePerBottle, isBn)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Stat Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(Strings.get("total_bottles", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Strings.get("bottles_count", isBn), Strings.formatNumber(summary.totalBottles, isBn)),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(Strings.get("cost", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Strings.formatCurrency(summary.currentCost, isBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(Strings.get("paid", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Strings.formatCurrency(summary.paidThisMonth, isBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DotPaymentGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Carry-over and Net Due
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Strings.get("carry_over", isBn),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Strings.formatCurrency(summary.carryOverDue, isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (summary.carryOverDue > 0) StatusAmberText else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Strings.get("net_due", isBn),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Strings.formatCurrency(summary.netDue, isBn),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.netDue > 0) StatusRedText else DotPaymentGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onQuickAdd(EntryType.MILK_PICKUP) },
                            colors = ButtonDefaults.buttonColors(containerColor = DotMilkBlue),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Strings.get("log_pickup", isBn), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onQuickAdd(EntryType.MILK_SKIP) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Strings.get("mark_skip", isBn), fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { onQuickAdd(EntryType.MILK_PAYMENT) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Strings.get("pay_farm", isBn), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Tabs: Pickups & Skips vs Payments
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(Strings.get("pickups_and_skips", isBn), fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(Strings.get("payments_history", isBn), fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        if (selectedTab == 0) {
            val allDeliveryItems = (uiState.milkPickupsThisMonth.map { PickupOrSkip.Pickup(it) } +
                    uiState.milkSkipsThisMonth.map { PickupOrSkip.Skip(it) })
                .sortedByDescending { it.date }

            if (allDeliveryItems.isEmpty()) {
                item {
                    Text(
                        text = Strings.get("no_pickups_month", isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(allDeliveryItems, key = { it.key }) { item ->
                    when (item) {
                        is PickupOrSkip.Pickup -> {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(DotMilkBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = DotMilkBlue, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${Strings.formatNumber(item.pickup.bottles, isBn)} ${if (isBn) "বোতল দুধ গ্রহণ" else "bottles"}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${Strings.formatDate(item.pickup.date, isBn)} • ${item.pickup.addedBy}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeletePickup(item.pickup.id) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is PickupOrSkip.Skip -> {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(DotSkipGrey.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = DotSkipGrey, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = Strings.get("milk_skipped_day", isBn),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DotSkipGrey
                                            )
                                            Text(
                                                text = "${Strings.formatDate(item.skip.date, isBn)} ${if (item.skip.note.isNotBlank()) "• ${item.skip.note}" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeleteSkip(item.skip.id) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Payments Tab
            if (uiState.milkPaymentsThisMonth.isEmpty()) {
                item {
                    Text(
                        text = Strings.get("no_payments_month", isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.milkPaymentsThisMonth, key = { it.id }) { payment ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DotPaymentGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = DotPaymentGreen, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = Strings.formatCurrency(payment.amount, isBn),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DotPaymentGreen
                                    )
                                    Text(
                                        text = "${Strings.formatDate(payment.date, isBn)} • ${payment.addedBy} ${if (payment.note.isNotBlank()) "(${payment.note})" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { onDeletePayment(payment.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class PickupOrSkip(val date: String, val key: String) {
    class Pickup(val pickup: MilkPickup) : PickupOrSkip(pickup.date, "p_${pickup.id}")
    class Skip(val skip: com.example.data.model.MilkSkip) : PickupOrSkip(skip.date, "s_${skip.id}")
}

@Composable
fun MonthSelectorCard(
    selectedMonth: String,
    isBn: Boolean,
    onMonthChanged: (String) -> Unit
) {
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")

    val parts = selectedMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val monthNum = parts.getOrNull(1)?.toIntOrNull() ?: 11
    val monthIdx = (monthNum - 1).coerceIn(0, 11)

    val monthName = if (isBn) monthNamesBn[monthIdx] else monthNamesEn[monthIdx]
    val yearStr = Strings.formatNumber(year, isBn)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newMonth = if (monthNum == 1) 12 else monthNum - 1
                val newYear = if (monthNum == 1) year - 1 else year
                onMonthChanged(String.format(Locale.US, "%04d-%02d", newYear, newMonth))
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = "$monthName $yearStr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = {
                val newMonth = if (monthNum == 12) 1 else monthNum + 1
                val newYear = if (monthNum == 12) year + 1 else year
                onMonthChanged(String.format(Locale.US, "%04d-%02d", newYear, newMonth))
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }
    }
}
