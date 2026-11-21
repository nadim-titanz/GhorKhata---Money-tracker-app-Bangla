package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.GhorKhataUiState

@Composable
fun SettingsScreen(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onToggleLanguage: (String) -> Unit,
    onToggleBengaliNumerals: (Boolean) -> Unit,
    onUpdateMilkPrice: (Double) -> Unit,
    onSetActiveUser: (String) -> Unit,
    onAddRefillItem: (name: String, nameBn: String, category: String, unit: String, usualPrice: Double) -> Unit,
    onJoinHousehold: (String) -> Unit,
    onCreateHousehold: (name: String, code: String, members: String) -> Unit,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current

    var showEditMilkPriceDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = Strings.get("settings_title", isBn),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 1. Language & Numerals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = Strings.get("language_setting", isBn),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = isBn,
                            onClick = { onToggleLanguage("bn") },
                            label = { Text("বাংলা") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isBn,
                            onClick = { onToggleLanguage("en") },
                            label = { Text("English") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("bengali_numerals", isBn),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.useBengaliNumerals,
                            onCheckedChange = { onToggleBengaliNumerals(it) }
                        )
                    }
                }
            }
        }

        // 2. Household Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("household_management", isBn),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Invite Code", uiState.household.inviteCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, Strings.get("code_copied", isBn), Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = uiState.household.inviteCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${Strings.get("household_name", isBn)}: ${uiState.household.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showJoinDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(Strings.get("join_household", isBn), fontSize = 12.sp)
                        }
                        FilledTonalButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(Strings.get("create_household", isBn), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Milk Rate
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                    Column {
                        Text(
                            text = Strings.get("milk_settings", isBn),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${Strings.get("bottle_price", isBn)}: ${Strings.formatCurrency(uiState.milkPricePerBottle, isBn)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showEditMilkPriceDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(Strings.get("edit", isBn), fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Refill Items Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("manage_refill_items", isBn),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showAddItemDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "নতুন পণ্য" else "Add Item", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    uiState.refillItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isBn) item.nameBn else item.name} (${item.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Strings.formatCurrency(item.usualPrice, isBn),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Clean Data Reset
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "সকল হিসাব মুছে ফেলুন" else "Clear All Entries",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isBn) "নতুন করে নিজের হিসাব শুরু করতে চান?" else "Remove all past logs and start fresh",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (isBn) "মুছুন" else "Clear", fontSize = 12.sp)
                    }
                }
            }
        }

        // Minimal App Info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ghor Khata • Simple Household Ledger",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(if (isBn) "সকল ডাটা মুছে ফেলবেন?" else "Clear All Entries?") },
            text = {
                Text(
                    if (isBn)
                        "সকল কেনাকাটা, দুধ ও বিলের হিসাব স্থায়ীভাবে মুছে যাবে। আপনি সম্পূর্ণ নতুন করে হিসাব শুরু করতে পারবেন।"
                    else
                        "All purchase, milk, and bill logs will be permanently cleared. You can start completely fresh."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirmDialog = false
                        Toast.makeText(context, if (isBn) "সকল ডাটা মুছে ফেলা হয়েছে" else "All logs cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Yes, Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(Strings.get("cancel", isBn))
                }
            }
        )
    }

    // Edit Milk Price Dialog
    if (showEditMilkPriceDialog) {
        var newPrice by remember { mutableStateOf(uiState.milkPricePerBottle.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showEditMilkPriceDialog = false },
            title = { Text(Strings.get("milk_bottle_rate", isBn)) },
            text = {
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("৳") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val p = newPrice.toDoubleOrNull() ?: uiState.milkPricePerBottle
                    onUpdateMilkPrice(p)
                    showEditMilkPriceDialog = false
                }) {
                    Text(Strings.get("save", isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMilkPriceDialog = false }) {
                    Text(Strings.get("cancel", isBn))
                }
            }
        )
    }

    // Add Refill Item Dialog
    if (showAddItemDialog) {
        var name by remember { mutableStateOf("") }
        var nameBn by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("1kg") }
        var usualPrice by remember { mutableStateOf("250") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(Strings.get("add_refill_item", isBn)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = { Text("নাম (বাংলা)") },
                        placeholder = { Text("যেমন: চিনি, ডাল") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (English)") },
                        placeholder = { Text("e.g. Sugar, Lentils") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (e.g. 1kg)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = usualPrice,
                            onValueChange = { usualPrice = it },
                            label = { Text("Price (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameBn.isNotBlank() || name.isNotBlank()) {
                        val p = usualPrice.toDoubleOrNull() ?: 200.0
                        val fNameBn = nameBn.ifBlank { name }
                        val fName = name.ifBlank { nameBn }
                        onAddRefillItem(fName, fNameBn, "Kitchen", unit, p)
                        showAddItemDialog = false
                    }
                }) {
                    Text(Strings.get("save", isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text(Strings.get("cancel", isBn))
                }
            }
        )
    }

    // Join Household Dialog
    if (showJoinDialog) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text(Strings.get("join_household", isBn)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isBn) "পরিবারের ৬ সংখ্যার কোড লিখুন" else "Enter 6-digit invite code")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(Strings.get("enter_code", isBn)) },
                        placeholder = { Text("GK-1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (code.isNotBlank()) {
                        onJoinHousehold(code.uppercase())
                        showJoinDialog = false
                    }
                }) {
                    Text(Strings.get("join", isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text(Strings.get("cancel", isBn))
                }
            }
        )
    }

    // Create Household Dialog
    if (showCreateDialog) {
        var hName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(Strings.get("create_household", isBn)) },
            text = {
                OutlinedTextField(
                    value = hName,
                    onValueChange = { hName = it },
                    label = { Text(Strings.get("household_name", isBn)) },
                    placeholder = { Text("যেমন: আমাদের ঘর") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (hName.isNotBlank()) {
                        val newCode = "GK-${(1000..9999).random()}"
                        onCreateHousehold(hName, newCode, uiState.activeUser)
                        showCreateDialog = false
                    }
                }) {
                    Text(Strings.get("create", isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(Strings.get("cancel", isBn))
                }
            }
        )
    }
}
