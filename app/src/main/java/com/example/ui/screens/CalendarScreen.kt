package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarDayEntry
import com.example.data.model.EntryType
import com.example.ui.i18n.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.GhorKhataUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    uiState: GhorKhataUiState,
    isBn: Boolean,
    onSelectDate: (String) -> Unit,
    onQuickAddForDate: (String) -> Unit,
    onDeleteEntry: (CalendarDayEntry) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonthIdx by remember { mutableStateOf(calendar.get(Calendar.MONTH)) } // 0-based

    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")

    val daysOfWeekEn = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysOfWeekBn = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")

    val viewMonthStr = String.format(Locale.US, "%04d-%02d", currentYear, currentMonthIdx + 1)

    // Calculate days in month and start day of week
    val calForCalc = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonthIdx)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = calForCalc.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val daysInMonth = calForCalc.getActualMaximum(Calendar.DAY_OF_MONTH)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (currentMonthIdx == 0) {
                                currentMonthIdx = 11
                                currentYear -= 1
                            } else {
                                currentMonthIdx -= 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        val monthName = if (isBn) monthNamesBn[currentMonthIdx] else monthNamesEn[currentMonthIdx]
                        val yearStr = Strings.formatNumber(currentYear, isBn)
                        Text(
                            text = "$monthName $yearStr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = {
                            if (currentMonthIdx == 11) {
                                currentMonthIdx = 0
                                currentYear += 1
                            } else {
                                currentMonthIdx += 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of Week Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val dowList = if (isBn) daysOfWeekBn else daysOfWeekEn
                        dowList.forEach { dow ->
                            Text(
                                text = dow,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid
                    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0..6) {
                                val cellIdx = row * 7 + col
                                val dayNum = cellIdx - firstDayOfWeek + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonthIdx + 1, dayNum)
                                    val isSelected = dateStr == uiState.selectedCalendarDate
                                    val dots = uiState.calendarMonthDatesWithTypes[dateStr] ?: emptySet()

                                    CalendarDayCell(
                                        dayNumber = dayNum,
                                        dateStr = dateStr,
                                        isSelected = isSelected,
                                        dots = dots,
                                        isBn = isBn,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSelectDate(dateStr) }
                                    )
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legend bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = DotRefillOrange, label = Strings.get("legend_refill", isBn))
                    LegendItem(color = DotMilkBlue, label = Strings.get("legend_pickup", isBn))
                    LegendItem(color = DotPaymentGreen, label = Strings.get("legend_payment", isBn))
                    LegendItem(color = DotBillRed, label = Strings.get("legend_bill_due", isBn))
                    LegendItem(color = DotSkipGrey, label = Strings.get("legend_skip", isBn))
                }
            }
        }

        // Selected Date Entries Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(
                        Strings.get("entries_for_day", isBn),
                        Strings.formatDate(uiState.selectedCalendarDate, isBn)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { onQuickAddForDate(uiState.selectedCalendarDate) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("calendar_add_entry_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "নতুন যোগ" else "Add Entry", fontSize = 12.sp)
                }
            }
        }

        if (uiState.calendarEntriesForSelectedDate.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = Strings.get("no_entries_day", isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(uiState.calendarEntriesForSelectedDate, key = { "${it.type}_${it.id}" }) { entry ->
                CalendarEntryCard(
                    entry = entry,
                    isBn = isBn,
                    onDelete = { onDeleteEntry(entry) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    dateStr: String,
    isSelected: Boolean,
    dots: Set<EntryType>,
    isBn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val isToday = dateStr == todayStr

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Strings.formatNumber(dayNumber, isBn),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White
                else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Dots row (max 4 visible dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp)
        ) {
            dots.take(4).forEach { type ->
                val dotColor = when (type) {
                    EntryType.REFILL -> DotRefillOrange
                    EntryType.MILK_PICKUP -> DotMilkBlue
                    EntryType.MILK_PAYMENT -> DotPaymentGreen
                    EntryType.BILL_PAYMENT -> DotBillRed
                    EntryType.MILK_SKIP -> DotSkipGrey
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalendarEntryCard(
    entry: CalendarDayEntry,
    isBn: Boolean,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val (icon, tint) = when (entry.type) {
                    EntryType.REFILL -> Icons.Default.LocalGasStation to DotRefillOrange
                    EntryType.MILK_PICKUP -> Icons.Default.LocalDrink to DotMilkBlue
                    EntryType.MILK_PAYMENT -> Icons.Default.Payments to DotPaymentGreen
                    EntryType.BILL_PAYMENT -> Icons.Default.ReceiptLong to DotBillRed
                    EntryType.MILK_SKIP -> Icons.Default.EventBusy to DotSkipGrey
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${entry.subtitle} ${if (entry.addedBy.isNotBlank()) "• ${entry.addedBy}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.amountOrQty,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
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
