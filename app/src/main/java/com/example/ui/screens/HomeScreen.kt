package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EntryType
import com.example.data.model.RefillItemStatus
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.GhorKhataUiState

@Composable
fun HomeScreen(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onQuickAdd: (EntryType) -> Unit,
    onNavigateToMilk: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleLanguage: () -> Unit,
    onMarkBillPaidQuick: (billId: Long, month: String, amount: Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Minimal Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.get("app_title", isBn),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = uiState.household.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimal Language Toggle Button
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("language_toggle_btn")
                    ) {
                        Text(
                            text = if (isBn) "EN" else "বাংলা",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Refills Section (Clean & Focused)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("refills_heading", isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = { onQuickAdd(EntryType.REFILL) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "কেনাকাটা" else "Add Refill", fontSize = 12.sp)
                }
            }
        }

        item {
            if (uiState.refillStatuses.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBn) "কোনো পণ্য নেই • সেটিংস থেকে পণ্য যোগ করুন" else "No items added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.refillStatuses, key = { it.item.id }) { status ->
                        MinimalRefillCard(
                            status = status,
                            isBn = isBn,
                            onLogRefill = { onQuickAdd(EntryType.REFILL) }
                        )
                    }
                }
            }
        }

        // 3. Farm Milk Status (Clean Minimal Card)
        item {
            MinimalMilkCard(
                uiState = uiState,
                isBn = isBn,
                onLogPickup = { onQuickAdd(EntryType.MILK_PICKUP) },
                onPayFarm = { onQuickAdd(EntryType.MILK_PAYMENT) },
                onViewFullMilk = onNavigateToMilk
            )
        }

        // 4. Upcoming Bills (Only shown if there are bills due)
        if (uiState.upcomingBills.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("bills_heading", isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToBills) {
                        Text(if (isBn) "সব বিল" else "View All", fontSize = 12.sp)
                    }
                }
            }

            items(uiState.upcomingBills, key = { it.bill.id }) { billStatus ->
                MinimalBillRow(
                    billStatus = billStatus,
                    isBn = isBn,
                    onMarkPaid = {
                        onMarkBillPaidQuick(billStatus.bill.id, uiState.currentMonth, billStatus.bill.amount)
                    }
                )
            }
        }

        // 5. Recent Activity (Only if entries exist)
        if (uiState.recentActivity.isNotEmpty()) {
            item {
                Text(
                    text = Strings.get("recent_activity", isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(uiState.recentActivity.take(4), key = { "${it.type}_${it.id}_${it.date}" }) { entry ->
                MinimalActivityRow(entry = entry, isBn = isBn)
            }
        } else {
            // Calm empty state tip
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBn)
                                "নতুন রিফিল, দুধ বা বিলের হিসাব যোগ করতে নিচের + বাটনে চাপ দিন"
                            else
                                "Tap the + button below to log your first refill, milk pickup, or bill.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalRefillCard(
    status: RefillItemStatus,
    isBn: Boolean,
    onLogRefill: () -> Unit
) {
    val itemName = if (isBn) status.item.nameBn else status.item.name

    val isOverdue = status.runsOutInDays != null && status.runsOutInDays <= 0
    val isLow = status.runsOutInDays != null && status.runsOutInDays in 1..5

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .width(170.dp)
            .clickable { onLogRefill() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (status.purchaseCount >= 2 && status.runsOutInDays != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverdue) StatusRedText
                                else if (isLow) StatusAmberText
                                else StatusGreenText
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${status.item.unit} • ${Strings.formatCurrency(status.item.usualPrice, isBn)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statusText = when {
                status.purchaseCount == 0 -> if (isBn) "কোনো হিসাব নেই" else "No purchases yet"
                status.purchaseCount == 1 -> String.format(Strings.get("last_bought_days_ago", isBn), Strings.formatNumber(status.daysSinceLastPurchase ?: 0, isBn))
                isOverdue -> {
                    val d = Strings.formatNumber(kotlin.math.abs(status.runsOutInDays ?: 0), isBn)
                    String.format(Strings.get("overdue", isBn), d)
                }
                else -> {
                    val d = Strings.formatNumber(status.runsOutInDays ?: 0, isBn)
                    String.format(Strings.get("runs_out_in", isBn), d)
                }
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isOverdue || isLow) FontWeight.Bold else FontWeight.Medium,
                color = if (isOverdue) StatusRedText else if (isLow) StatusAmberText else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MinimalMilkCard(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onLogPickup: () -> Unit,
    onPayFarm: () -> Unit,
    onViewFullMilk: () -> Unit
) {
    val summary = uiState.milkSummary

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DotMilkBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalDrink, contentDescription = null, tint = DotMilkBlue, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("milk_heading", isBn),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onViewFullMilk,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(if (isBn) "বিস্তারিত" else "Details", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.get("total_bottles", isBn),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Strings.formatNumber(summary.totalBottles, isBn)} ${if (isBn) "বোতল" else "bottles"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Strings.get("due", isBn),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Strings.formatCurrency(summary.netDue, isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netDue > 0) StatusRedText else DotPaymentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLogPickup,
                    colors = ButtonDefaults.buttonColors(containerColor = DotMilkBlue),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("log_pickup", isBn), fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onPayFarm,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(Strings.get("pay_farm", isBn), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MinimalBillRow(
    billStatus: com.example.data.model.BillStatus,
    isBn: Boolean,
    onMarkPaid: () -> Unit
) {
    val bill = billStatus.bill
    val isOverdue = billStatus.isOverdue

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) bill.nameBn else bill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${Strings.formatCurrency(bill.amount, isBn)} • ${
                        if (isOverdue) {
                            val d = Strings.formatNumber(kotlin.math.abs(billStatus.daysUntilDue), isBn)
                            String.format(Strings.get("overdue_by_days", isBn), d)
                        } else {
                            val d = Strings.formatNumber(billStatus.daysUntilDue, isBn)
                            String.format(Strings.get("due_in_days", isBn), d)
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) StatusRedText else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onMarkPaid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(Strings.get("mark_paid", isBn), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MinimalActivityRow(
    entry: com.example.data.model.CalendarDayEntry,
    isBn: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tint = when (entry.type) {
                EntryType.REFILL -> DotRefillOrange
                EntryType.MILK_PICKUP -> DotMilkBlue
                EntryType.MILK_PAYMENT -> DotPaymentGreen
                EntryType.BILL_PAYMENT -> DotBillRed
                EntryType.MILK_SKIP -> DotSkipGrey
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = entry.amountOrQty,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}
