package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.EntryType
import com.example.data.model.RefillItem
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    initialType: EntryType = EntryType.REFILL,
    initialDate: String? = null,
    isBn: Boolean,
    refillItems: List<RefillItem>,
    bills: List<Bill>,
    suggestedMilkBottles: Int,
    activeUser: String,
    onDismiss: () -> Unit,
    onSaveRefill: (itemId: Long, date: String, qty: Double, price: Double, note: String) -> Unit,
    onSaveMilkPickup: (date: String, bottles: Int) -> Unit,
    onSaveMilkPayment: (date: String, amount: Double, note: String) -> Unit,
    onSaveBillPayment: (billId: Long, month: String, amount: Double, date: String) -> Unit,
    onSaveMilkSkip: (date: String, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(initialType) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    var entryDate by remember { mutableStateOf(initialDate?.ifBlank { todayStr } ?: todayStr) }

    // Refill Form States
    var selectedRefillItem by remember { mutableStateOf(refillItems.firstOrNull()) }
    var refillQty by remember { mutableStateOf("1") }
    var refillPrice by remember(selectedRefillItem) {
        mutableStateOf(selectedRefillItem?.usualPrice?.toInt()?.toString() ?: "1400")
    }
    var refillNote by remember { mutableStateOf("") }

    // Milk Pickup Form States
    var milkBottles by remember(suggestedMilkBottles) {
        mutableStateOf(suggestedMilkBottles.toString())
    }

    // Milk Payment Form States
    var milkPayAmount by remember { mutableStateOf("1000") }
    var milkPayNote by remember { mutableStateOf("") }

    // Milk Skip Form States
    var milkSkipNote by remember { mutableStateOf("") }

    // Bill Payment Form States
    var selectedBill by remember { mutableStateOf(bills.firstOrNull()) }
    var billAmount by remember(selectedBill) {
        mutableStateOf(selectedBill?.amount?.toInt()?.toString() ?: "2450")
    }
    var billMonth by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM", Locale.US).format(Date()))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_add_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = Strings.get("quick_add_title", isBn),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Type Selection Row (Scrollable or 4 grid chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTypeChip(
                    label = if (isBn) "রিফিল" else "Refill",
                    icon = Icons.Default.LocalGasStation,
                    isSelected = selectedType == EntryType.REFILL,
                    activeColor = DotRefillOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = EntryType.REFILL }
                )
                QuickTypeChip(
                    label = if (isBn) "দুধ নেওয়া" else "Milk",
                    icon = Icons.Default.LocalDrink,
                    isSelected = selectedType == EntryType.MILK_PICKUP,
                    activeColor = DotMilkBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = EntryType.MILK_PICKUP }
                )
                QuickTypeChip(
                    label = if (isBn) "দুধের টাকা" else "Payment",
                    icon = Icons.Default.Payments,
                    isSelected = selectedType == EntryType.MILK_PAYMENT,
                    activeColor = DotPaymentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = EntryType.MILK_PAYMENT }
                )
                QuickTypeChip(
                    label = if (isBn) "বিল" else "Bill",
                    icon = Icons.Default.ReceiptLong,
                    isSelected = selectedType == EntryType.BILL_PAYMENT,
                    activeColor = DotBillRed,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = EntryType.BILL_PAYMENT }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Common Date Field
            OutlinedTextField(
                value = entryDate,
                onValueChange = { entryDate = it },
                label = { Text(Strings.get("date", isBn) + " (yyyy-mm-dd)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_date_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedType) {
                EntryType.REFILL -> {
                    // Refill Item Selector
                    Text(
                        text = Strings.get("select_item", isBn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        refillItems.take(3).forEach { item ->
                            val isSel = selectedRefillItem?.id == item.id
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedRefillItem = item
                                    refillPrice = item.usualPrice.toInt().toString()
                                },
                                label = { Text(if (isBn) item.nameBn else item.name) }
                            )
                        }
                    }
                    if (refillItems.size > 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            refillItems.drop(3).forEach { item ->
                                val isSel = selectedRefillItem?.id == item.id
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        selectedRefillItem = item
                                        refillPrice = item.usualPrice.toInt().toString()
                                    },
                                    label = { Text(if (isBn) item.nameBn else item.name) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = refillQty,
                            onValueChange = { refillQty = it },
                            label = { Text(Strings.get("quantity", isBn)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = refillPrice,
                            onValueChange = { refillPrice = it },
                            label = { Text(Strings.get("price", isBn) + " (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = refillNote,
                        onValueChange = { refillNote = it },
                        label = { Text(Strings.get("note_optional", isBn)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                EntryType.MILK_PICKUP -> {
                    // Suggested bottle count indicator
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PineTealSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBn)
                                    "পূর্ববর্তী সংগ্রহের পর থেকে হিসাবকৃত বোতল: $suggestedMilkBottles টি (পরিবর্তনযোগ্য)"
                                else
                                    "Default based on days since last pickup: $suggestedMilkBottles bottles (editable)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = milkBottles,
                        onValueChange = { milkBottles = it },
                        label = { Text(Strings.get("bottles", isBn)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            selectedType = EntryType.MILK_SKIP
                        }
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = DotSkipGrey)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "দুধ আসেনি? ছুটির দিন চিহ্নিত করুন" else "No milk delivered today? Mark Skip",
                            color = DotSkipGrey
                        )
                    }
                }

                EntryType.MILK_PAYMENT -> {
                    OutlinedTextField(
                        value = milkPayAmount,
                        onValueChange = { milkPayAmount = it },
                        label = { Text(Strings.get("amount", isBn) + " (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = milkPayNote,
                        onValueChange = { milkPayNote = it },
                        label = { Text(if (isBn) "পেমেন্ট মাধ্যম (বিকাশ / নগদ / ক্যাশ)" else "Payment method (bKash / Cash)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                EntryType.MILK_SKIP -> {
                    Text(
                        text = if (isBn) "খামার থেকে দুধ আসেনি এমন দিন চিহ্নিত করুন" else "Mark day when milk delivery was skipped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = milkSkipNote,
                        onValueChange = { milkSkipNote = it },
                        label = { Text(Strings.get("note_optional", isBn)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                EntryType.BILL_PAYMENT -> {
                    Text(
                        text = if (isBn) "কোন বিল পরিশোধ করেছেন?" else "Select bill to pay",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    bills.filter { it.active }.forEach { bill ->
                        val isSel = selectedBill?.id == bill.id
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedBill = bill
                                billAmount = bill.amount.toInt().toString()
                            },
                            label = { Text(if (isBn) "${bill.nameBn} (৳${bill.amount.toInt()})" else "${bill.name} (৳${bill.amount.toInt()})") },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = billAmount,
                            onValueChange = { billAmount = it },
                            label = { Text(Strings.get("amount", isBn) + " (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = billMonth,
                            onValueChange = { billMonth = it },
                            label = { Text(if (isBn) "মাস (yyyy-MM)" else "Month (yyyy-MM)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save and Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(Strings.get("cancel", isBn))
                }

                Button(
                    onClick = {
                        when (selectedType) {
                            EntryType.REFILL -> {
                                selectedRefillItem?.let { item ->
                                    val q = refillQty.toDoubleOrNull() ?: 1.0
                                    val p = refillPrice.toDoubleOrNull() ?: item.usualPrice
                                    onSaveRefill(item.id, entryDate, q, p, refillNote)
                                }
                            }
                            EntryType.MILK_PICKUP -> {
                                val b = milkBottles.toIntOrNull() ?: suggestedMilkBottles
                                onSaveMilkPickup(entryDate, b)
                            }
                            EntryType.MILK_PAYMENT -> {
                                val a = milkPayAmount.toDoubleOrNull() ?: 0.0
                                onSaveMilkPayment(entryDate, a, milkPayNote)
                            }
                            EntryType.MILK_SKIP -> {
                                onSaveMilkSkip(entryDate, milkSkipNote)
                            }
                            EntryType.BILL_PAYMENT -> {
                                selectedBill?.let { bill ->
                                    val a = billAmount.toDoubleOrNull() ?: bill.amount
                                    onSaveBillPayment(bill.id, billMonth, a, entryDate)
                                }
                            }
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("quick_add_submit_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.get("save", isBn), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, activeColor) else null,
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
