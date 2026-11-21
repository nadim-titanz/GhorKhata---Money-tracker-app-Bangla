package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.GhorKhataUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillsScreen(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onSelectMonth: (String) -> Unit,
    onMarkPaid: (billId: Long, month: String, amount: Double, date: String) -> Unit,
    onDeletePayment: (Long) -> Unit,
    onToggleActive: (Bill) -> Unit,
    onAddNewBill: (name: String, nameBn: String, amount: Double, dueDay: Int) -> Unit
) {
    var showAddBillDialog by remember { mutableStateOf(false) }
    var payingBillStatus by remember { mutableStateOf<BillStatus?>(null) }

    val totalBillsCount = uiState.billStatuses.size
    val paidCount = uiState.billStatuses.count { it.isPaidThisMonth }
    val pendingCount = totalBillsCount - paidCount
    val totalAmount = uiState.billStatuses.sumOf { it.bill.amount }
    val paidAmount = uiState.billStatuses.filter { it.isPaidThisMonth }.sumOf { it.payment?.amount ?: it.bill.amount }
    val pendingAmount = totalAmount - paidAmount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bills_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            MonthSelectorCard(
                selectedMonth = uiState.selectedMonth,
                isBn = isBn,
                onMonthChanged = onSelectMonth
            )
        }

        // Bills Overview Banner
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
                            text = Strings.get("bills_summary", isBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { showAddBillDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "নতুন বিল" else "New Bill", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(Strings.get("total_bills", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${Strings.formatNumber(totalBillsCount, isBn)} (${Strings.formatCurrency(totalAmount, isBn)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(Strings.get("total_paid", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${Strings.formatNumber(paidCount, isBn)} (${Strings.formatCurrency(paidAmount, isBn)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DotPaymentGreen
                            )
                        }

                        Column {
                            Text(Strings.get("total_pending", isBn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${Strings.formatNumber(pendingCount, isBn)} (${Strings.formatCurrency(pendingAmount, isBn)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingAmount > 0) StatusRedText else DotPaymentGreen
                            )
                        }
                    }
                }
            }
        }

        // Bills List
        items(uiState.billStatuses, key = { it.bill.id }) { status ->
            BillItemCard(
                status = status,
                isBn = isBn,
                onTapPay = { payingBillStatus = status },
                onDeletePayment = {
                    status.payment?.let { onDeletePayment(it.id) }
                }
            )
        }
    }

    // Pay Bill Dialog
    payingBillStatus?.let { status ->
        PayBillDialog(
            bill = status.bill,
            month = uiState.selectedMonth,
            isBn = isBn,
            onDismiss = { payingBillStatus = null },
            onConfirm = { amount, date ->
                onMarkPaid(status.bill.id, uiState.selectedMonth, amount, date)
                payingBillStatus = null
            }
        )
    }

    // Add New Bill Dialog
    if (showAddBillDialog) {
        AddNewBillDialog(
            isBn = isBn,
            onDismiss = { showAddBillDialog = false },
            onSave = { name, nameBn, amount, dueDay ->
                onAddNewBill(name, nameBn, amount, dueDay)
                showAddBillDialog = false
            }
        )
    }
}

@Composable
private fun BillItemCard(
    status: BillStatus,
    isBn: Boolean,
    onTapPay: () -> Unit,
    onDeletePayment: () -> Unit
) {
    val bill = status.bill
    val isPaid = status.isPaidThisMonth
    val isOverdue = status.isOverdue

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else if (isOverdue) StatusRedBg.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPaid) DotPaidGrey.copy(alpha = 0.15f)
                                else if (isOverdue) DotBillRed.copy(alpha = 0.2f)
                                else DotBillRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (isPaid) DotPaidGrey else DotBillRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isBn) bill.nameBn else bill.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Strings.get("due_on", isBn), Strings.formatNumber(bill.dueDay, isBn)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = Strings.formatCurrency(bill.amount, isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusGreenBg
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DotPaymentGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Strings.get("paid_on", isBn), Strings.formatDate(status.payment?.paidDate ?: "", isBn)),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreenText
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDeletePayment) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Undo payment",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    val statusText = if (isOverdue) {
                        val d = Strings.formatNumber(kotlin.math.abs(status.daysUntilDue), isBn)
                        String.format(Strings.get("overdue_by_days", isBn), d)
                    } else {
                        val d = Strings.formatNumber(status.daysUntilDue, isBn)
                        String.format(Strings.get("due_in_days", isBn), d)
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) StatusRedText else StatusAmberText
                    )

                    Button(
                        onClick = onTapPay,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.get("mark_paid", isBn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PayBillDialog(
    bill: Bill,
    month: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: String) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var payAmount by remember { mutableStateOf(bill.amount.toInt().toString()) }
    var payDate by remember { mutableStateOf(todayStr) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "${bill.nameBn} পরিশোধ" else "Pay ${bill.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = payAmount,
                    onValueChange = { payAmount = it },
                    label = { Text(Strings.get("amount", isBn) + " (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = payDate,
                    onValueChange = { payDate = it },
                    label = { Text(Strings.get("date", isBn) + " (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val a = payAmount.toDoubleOrNull() ?: bill.amount
                onConfirm(a, payDate)
            }) {
                Text(Strings.get("save", isBn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.get("cancel", isBn))
            }
        }
    )
}

@Composable
private fun AddNewBillDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, nameBn: String, amount: Double, dueDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("add_new_bill", isBn)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(Strings.get("bill_name_bn", isBn)) },
                    placeholder = { Text("যেমন: ওয়াসা বিল, কাজের বুয়ার বেতন") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("bill_name", isBn)) },
                    placeholder = { Text("e.g. WASA Bill, Maid Salary") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(Strings.get("bill_amount", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDay,
                    onValueChange = { dueDay = it },
                    label = { Text(Strings.get("due_day", isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nameBn.isNotBlank() || name.isNotBlank()) {
                    val a = amount.toDoubleOrNull() ?: 1000.0
                    val d = dueDay.toIntOrNull() ?: 10
                    val finalNameBn = nameBn.ifBlank { name }
                    val finalName = name.ifBlank { nameBn }
                    onSave(finalName, finalNameBn, a, d)
                }
            }) {
                Text(Strings.get("save", isBn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.get("cancel", isBn))
            }
        }
    )
}
