package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GhorKhataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class GhorKhataUiState(
    val language: String = "bn",
    val activeUser: String = "Nadim",
    val milkPricePerBottle: Double = 90.0,
    val useBengaliNumerals: Boolean = true,
    val household: HouseholdEntity = HouseholdEntity(name = "খান পরিবার", inviteCode = "GK-8492", members = "Nadim, Tasnim, Ayaan"),
    val refillItems: List<RefillItem> = emptyList(),
    val refillStatuses: List<RefillItemStatus> = emptyList(),
    val milkSummary: MilkMonthlySummary = MilkMonthlySummary(
        month = "",
        totalBottles = 0,
        pricePerBottle = 90.0,
        currentCost = 0.0,
        carryOverDue = 0.0,
        paidThisMonth = 0.0,
        netDue = 0.0,
        daysSinceLastPickup = null
    ),
    val milkPickupsThisMonth: List<MilkPickup> = emptyList(),
    val milkPaymentsThisMonth: List<MilkPayment> = emptyList(),
    val milkSkipsThisMonth: List<MilkSkip> = emptyList(),
    val suggestedMilkPickupBottles: Int = 1,
    val bills: List<Bill> = emptyList(),
    val billStatuses: List<BillStatus> = emptyList(),
    val upcomingBills: List<BillStatus> = emptyList(),
    val selectedCalendarDate: String = "",
    val calendarEntriesForSelectedDate: List<CalendarDayEntry> = emptyList(),
    val calendarMonthDatesWithTypes: Map<String, Set<EntryType>> = emptyMap(),
    val currentMonth: String = "",
    val selectedMonth: String = "",
    val recentActivity: List<CalendarDayEntry> = emptyList()
)

class GhorKhataViewModel(
    private val repository: GhorKhataRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    private val _calendarViewingMonth = MutableStateFlow(monthFormat.format(Date()))
    val calendarViewingMonth: StateFlow<String> = _calendarViewingMonth.asStateFlow()

    val uiState: StateFlow<GhorKhataUiState> = combine(
        repository.appSettings,
        repository.household,
        repository.allRefillItems,
        repository.allPurchases,
        repository.allMilkPickups,
        repository.allMilkPayments,
        repository.allMilkSkips,
        repository.allBills,
        repository.allBillPayments,
        _selectedMonth,
        _selectedCalendarDate,
        _calendarViewingMonth
    ) { args: Array<Any?> ->
        val settings = args[0] as? AppSettingsEntity
        val household = (args[1] as? HouseholdEntity) ?: HouseholdEntity(name = "খান পরিবার", inviteCode = "GK-8492", members = "Nadim, Tasnim, Ayaan")
        @Suppress("UNCHECKED_CAST") val refillItems = (args[2] as? List<RefillItem>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val purchases = (args[3] as? List<Purchase>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val milkPickups = (args[4] as? List<MilkPickup>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val milkPayments = (args[5] as? List<MilkPayment>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val milkSkips = (args[6] as? List<MilkSkip>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val bills = (args[7] as? List<Bill>) ?: emptyList()
        @Suppress("UNCHECKED_CAST") val billPayments = (args[8] as? List<BillPayment>) ?: emptyList()
        val selMonth = (args[9] as? String) ?: monthFormat.format(Date())
        val selCalDate = (args[10] as? String) ?: dateFormat.format(Date())
        val calViewMonth = (args[11] as? String) ?: monthFormat.format(Date())

        val isBn = settings?.language != "en"
        val activeUser = settings?.activeUser ?: "Nadim"
        val milkPrice = settings?.milkPricePerBottle ?: 90.0
        val useBengaliNumerals = settings?.useBengaliNumerals ?: true
        val todayStr = dateFormat.format(Date())
        val currentMonthStr = monthFormat.format(Date())

        // 1. Compute Refill Item Statuses
        val refillStatuses = refillItems.map { item ->
            val itemPurchases = purchases.filter { it.itemId == item.id }.sortedBy { it.date }
            val count = itemPurchases.size
            if (count == 0) {
                RefillItemStatus(
                    item = item,
                    lastPurchase = null,
                    daysSinceLastPurchase = null,
                    averageDaysBetweenPurchases = null,
                    runsOutInDays = null,
                    purchaseCount = 0
                )
            } else {
                val lastPurchase = itemPurchases.last()
                val daysSince = daysBetween(lastPurchase.date, todayStr)

                if (count == 1) {
                    RefillItemStatus(
                        item = item,
                        lastPurchase = lastPurchase,
                        daysSinceLastPurchase = daysSince,
                        averageDaysBetweenPurchases = null,
                        runsOutInDays = null,
                        purchaseCount = 1
                    )
                } else {
                    // Average of last up to 3 gaps
                    val gaps = mutableListOf<Int>()
                    for (i in 0 until count - 1) {
                        gaps.add(daysBetween(itemPurchases[i].date, itemPurchases[i + 1].date))
                    }
                    val last3Gaps = gaps.takeLast(3)
                    val avgGap = if (last3Gaps.isNotEmpty()) {
                        (last3Gaps.sum().toDouble() / last3Gaps.size).toInt()
                    } else 30
                    val runsOutIn = avgGap - daysSince

                    RefillItemStatus(
                        item = item,
                        lastPurchase = lastPurchase,
                        daysSinceLastPurchase = daysSince,
                        averageDaysBetweenPurchases = avgGap,
                        runsOutInDays = runsOutIn,
                        purchaseCount = count
                    )
                }
            }
        }

        // 2. Compute Milk Monthly Summary
        val pickupsThisMonth = milkPickups.filter { it.date.startsWith(selMonth) }
        val paymentsThisMonth = milkPayments.filter { it.date.startsWith(selMonth) }
        val skipsThisMonth = milkSkips.filter { it.date.startsWith(selMonth) }
        val totalBottlesThisMonth = pickupsThisMonth.sumOf { it.bottles }
        val costThisMonth = totalBottlesThisMonth * milkPrice
        val paidThisMonth = paymentsThisMonth.sumOf { it.amount }

        // Previous months carry-over:
        val prevPickups = milkPickups.filter { it.date < "$selMonth-01" }
        val prevPayments = milkPayments.filter { it.date < "$selMonth-01" }
        val prevCost = prevPickups.sumOf { it.bottles } * milkPrice
        val prevPaid = prevPayments.sumOf { it.amount }
        val carryOverDue = maxOf(0.0, prevCost - prevPaid)
        val netDue = (costThisMonth + carryOverDue) - paidThisMonth

        val latestPickupOverall = milkPickups.maxByOrNull { it.date }
        val daysSinceLastPickup = latestPickupOverall?.let { daysBetween(it.date, todayStr) }

        // Default bottles for next pickup: days since last pickup minus skips since then
        val suggestedBottles = if (latestPickupOverall != null) {
            val daysPassed = daysBetween(latestPickupOverall.date, todayStr)
            val skipsSince = milkSkips.count { it.date > latestPickupOverall.date && it.date <= todayStr }
            maxOf(1, daysPassed - skipsSince)
        } else {
            1
        }

        val milkSummary = MilkMonthlySummary(
            month = selMonth,
            totalBottles = totalBottlesThisMonth,
            pricePerBottle = milkPrice,
            currentCost = costThisMonth,
            carryOverDue = carryOverDue,
            paidThisMonth = paidThisMonth,
            netDue = netDue,
            daysSinceLastPickup = daysSinceLastPickup
        )

        // 3. Compute Bills Statuses for selectedMonth
        val paymentsForMonth = billPayments.filter { it.month == selMonth }
        val billStatuses = bills.filter { it.active }.map { bill ->
            val payment = paymentsForMonth.find { it.billId == bill.id }
            val isPaid = payment != null
            val dueDateStr = buildDueDateString(selMonth, bill.dueDay)
            val daysUntilDue = daysBetween(todayStr, dueDateStr)
            val isOverdue = !isPaid && (todayStr > dueDateStr)

            BillStatus(
                bill = bill,
                isPaidThisMonth = isPaid,
                payment = payment,
                dueDueDateThisMonth = dueDateStr,
                daysUntilDue = daysUntilDue,
                isOverdue = isOverdue
            )
        }

        // Upcoming bills for Home card: active unpaid bills due within 5 days or overdue in current month
        val currentMonthBillPayments = billPayments.filter { it.month == currentMonthStr }
        val upcomingBills = bills.filter { it.active }.mapNotNull { bill ->
            val payment = currentMonthBillPayments.find { it.billId == bill.id }
            if (payment != null) null
            else {
                val dueDateStr = buildDueDateString(currentMonthStr, bill.dueDay)
                val daysUntilDue = daysBetween(todayStr, dueDateStr)
                val isOverdue = todayStr > dueDateStr
                if (daysUntilDue in 0..5 || isOverdue) {
                    BillStatus(
                        bill = bill,
                        isPaidThisMonth = false,
                        payment = null,
                        dueDueDateThisMonth = dueDateStr,
                        daysUntilDue = daysUntilDue,
                        isOverdue = isOverdue
                    )
                } else null
            }
        }.sortedBy { it.daysUntilDue }

        // 4. Calendar Entries for selected date
        val entriesForSelDate = mutableListOf<CalendarDayEntry>()
        purchases.filter { it.date == selCalDate }.forEach { p ->
            val itemName = refillItems.find { it.id == p.itemId }?.let { if (isBn) it.nameBn else it.name } ?: "Refill"
            entriesForSelDate.add(
                CalendarDayEntry(
                    id = p.id,
                    type = EntryType.REFILL,
                    title = itemName,
                    subtitle = if (p.note.isNotBlank()) p.note else "Qty: ${p.qty}",
                    amountOrQty = "৳${p.price.toInt()}",
                    addedBy = p.addedBy,
                    date = p.date
                )
            )
        }
        milkPickups.filter { it.date == selCalDate }.forEach { mp ->
            entriesForSelDate.add(
                CalendarDayEntry(
                    id = mp.id,
                    type = EntryType.MILK_PICKUP,
                    title = if (isBn) "দুধ সংগ্রহ" else "Milk Pickup",
                    subtitle = if (isBn) "${mp.bottles} বোতল দুধ গ্রহণ" else "${mp.bottles} bottles received",
                    amountOrQty = "${mp.bottles} btl",
                    addedBy = mp.addedBy,
                    date = mp.date
                )
            )
        }
        milkPayments.filter { it.date == selCalDate }.forEach { pay ->
            entriesForSelDate.add(
                CalendarDayEntry(
                    id = pay.id,
                    type = EntryType.MILK_PAYMENT,
                    title = if (isBn) "দুধের বিল পরিশোধ" else "Milk Payment",
                    subtitle = if (pay.note.isNotBlank()) pay.note else (if (isBn) "খামারে টাকা পরিশোধ" else "Payment to farm"),
                    amountOrQty = "৳${pay.amount.toInt()}",
                    addedBy = pay.addedBy,
                    date = pay.date
                )
            )
        }
        milkSkips.filter { it.date == selCalDate }.forEach { skip ->
            entriesForSelDate.add(
                CalendarDayEntry(
                    id = skip.id,
                    type = EntryType.MILK_SKIP,
                    title = if (isBn) "দুধ বন্ধ / ছুটি" else "Milk Skipped",
                    subtitle = if (skip.note.isNotBlank()) skip.note else (if (isBn) "দুধ আসেনি" else "No milk delivered"),
                    amountOrQty = "0 btl",
                    addedBy = "",
                    date = skip.date
                )
            )
        }
        // Bills due on this date in current month or calendar month
        val dateMonth = if (selCalDate.length >= 7) selCalDate.substring(0, 7) else calViewMonth
        val dayNum = selCalDate.takeLast(2).toIntOrNull() ?: -1
        bills.filter { it.active && it.dueDay == dayNum }.forEach { bill ->
            val paid = billPayments.any { it.billId == bill.id && it.month == dateMonth }
            entriesForSelDate.add(
                CalendarDayEntry(
                    id = bill.id,
                    type = EntryType.BILL_PAYMENT,
                    title = if (isBn) bill.nameBn else bill.name,
                    subtitle = if (paid) (if (isBn) "পরিশোধিত" else "Paid") else (if (isBn) "বকেয়া বিল" else "Unpaid Bill"),
                    amountOrQty = "৳${bill.amount.toInt()}",
                    addedBy = if (paid) "Paid" else "Due",
                    date = selCalDate
                )
            )
        }

        // 5. Calendar Month Map for dots
        val calMonthMap = mutableMapOf<String, MutableSet<EntryType>>()
        purchases.filter { it.date.startsWith(calViewMonth) }.forEach {
            calMonthMap.getOrPut(it.date) { mutableSetOf() }.add(EntryType.REFILL)
        }
        milkPickups.filter { it.date.startsWith(calViewMonth) }.forEach {
            calMonthMap.getOrPut(it.date) { mutableSetOf() }.add(EntryType.MILK_PICKUP)
        }
        milkPayments.filter { it.date.startsWith(calViewMonth) }.forEach {
            calMonthMap.getOrPut(it.date) { mutableSetOf() }.add(EntryType.MILK_PAYMENT)
        }
        milkSkips.filter { it.date.startsWith(calViewMonth) }.forEach {
            calMonthMap.getOrPut(it.date) { mutableSetOf() }.add(EntryType.MILK_SKIP)
        }
        bills.filter { it.active }.forEach { bill ->
            val billDueDate = buildDueDateString(calViewMonth, bill.dueDay)
            val isPaid = billPayments.any { it.billId == bill.id && it.month == calViewMonth }
            calMonthMap.getOrPut(billDueDate) { mutableSetOf() }.add(EntryType.BILL_PAYMENT)
        }

        // 6. Recent Activity Feed for Home
        val allActivity = mutableListOf<CalendarDayEntry>()
        purchases.take(4).forEach { p ->
            val item = refillItems.find { it.id == p.itemId }
            val name = if (isBn) (item?.nameBn ?: "রিফিল") else (item?.name ?: "Refill")
            allActivity.add(
                CalendarDayEntry(
                    id = p.id,
                    type = EntryType.REFILL,
                    title = "$name (কেনা হয়েছে)",
                    subtitle = "৳${p.price.toInt()} • ${p.qty} ${item?.unit ?: ""}",
                    amountOrQty = "৳${p.price.toInt()}",
                    addedBy = p.addedBy,
                    date = p.date
                )
            )
        }
        milkPickups.take(3).forEach { mp ->
            allActivity.add(
                CalendarDayEntry(
                    id = mp.id,
                    type = EntryType.MILK_PICKUP,
                    title = if (isBn) "দুধ সংগ্রহ" else "Milk Pickup",
                    subtitle = "${mp.bottles} বোতল",
                    amountOrQty = "${mp.bottles} btl",
                    addedBy = mp.addedBy,
                    date = mp.date
                )
            )
        }
        milkPayments.take(2).forEach { pay ->
            allActivity.add(
                CalendarDayEntry(
                    id = pay.id,
                    type = EntryType.MILK_PAYMENT,
                    title = if (isBn) "দুধের বিল পরিশোধ" else "Milk Payment",
                    subtitle = pay.note.ifBlank { "Farm payment" },
                    amountOrQty = "৳${pay.amount.toInt()}",
                    addedBy = pay.addedBy,
                    date = pay.date
                )
            )
        }
        billPayments.take(2).forEach { bp ->
            val bill = bills.find { it.id == bp.billId }
            val bName = if (isBn) (bill?.nameBn ?: "বিল") else (bill?.name ?: "Bill")
            allActivity.add(
                CalendarDayEntry(
                    id = bp.id,
                    type = EntryType.BILL_PAYMENT,
                    title = "$bName পরিশোধ",
                    subtitle = bp.month,
                    amountOrQty = "৳${bp.amount.toInt()}",
                    addedBy = bp.addedBy,
                    date = bp.paidDate
                )
            )
        }
        allActivity.sortByDescending { it.date }

        GhorKhataUiState(
            language = settings?.language ?: "bn",
            activeUser = activeUser,
            milkPricePerBottle = milkPrice,
            useBengaliNumerals = useBengaliNumerals,
            household = household,
            refillItems = refillItems,
            refillStatuses = refillStatuses,
            milkSummary = milkSummary,
            milkPickupsThisMonth = pickupsThisMonth,
            milkPaymentsThisMonth = paymentsThisMonth,
            milkSkipsThisMonth = skipsThisMonth,
            suggestedMilkPickupBottles = suggestedBottles,
            bills = bills,
            billStatuses = billStatuses,
            upcomingBills = upcomingBills,
            selectedCalendarDate = selCalDate,
            calendarEntriesForSelectedDate = entriesForSelDate,
            calendarMonthDatesWithTypes = calMonthMap,
            currentMonth = currentMonthStr,
            selectedMonth = selMonth,
            recentActivity = allActivity.take(6)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GhorKhataUiState()
    )

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    fun selectCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun selectCalendarViewingMonth(month: String) {
        _calendarViewingMonth.value = month
    }

    // CRUD Actions
    fun logPurchase(itemId: Long, date: String, qty: Double, price: Double, note: String) {
        viewModelScope.launch {
            val user = uiState.value.activeUser
            repository.insertPurchase(
                Purchase(
                    itemId = itemId,
                    date = date,
                    qty = qty,
                    price = price,
                    addedBy = user,
                    note = note
                )
            )
        }
    }

    fun deletePurchase(purchaseId: Long) {
        viewModelScope.launch {
            repository.deletePurchase(Purchase(id = purchaseId, itemId = 0, date = "", qty = 0.0, price = 0.0, addedBy = ""))
        }
    }

    fun logMilkPickup(date: String, bottles: Int) {
        viewModelScope.launch {
            val user = uiState.value.activeUser
            repository.insertMilkPickup(
                MilkPickup(
                    date = date,
                    bottles = bottles,
                    addedBy = user
                )
            )
        }
    }

    fun deleteMilkPickup(id: Long) {
        viewModelScope.launch {
            repository.deleteMilkPickup(MilkPickup(id = id, date = "", bottles = 0, addedBy = ""))
        }
    }

    fun logMilkPayment(date: String, amount: Double, note: String) {
        viewModelScope.launch {
            val user = uiState.value.activeUser
            repository.insertMilkPayment(
                MilkPayment(
                    date = date,
                    amount = amount,
                    addedBy = user,
                    note = note
                )
            )
        }
    }

    fun deleteMilkPayment(id: Long) {
        viewModelScope.launch {
            repository.deleteMilkPayment(MilkPayment(id = id, date = "", amount = 0.0, addedBy = ""))
        }
    }

    fun logMilkSkip(date: String, note: String) {
        viewModelScope.launch {
            repository.insertMilkSkip(
                MilkSkip(
                    date = date,
                    note = note
                )
            )
        }
    }

    fun deleteMilkSkip(id: Long) {
        viewModelScope.launch {
            repository.deleteMilkSkip(MilkSkip(id = id, date = "", note = ""))
        }
    }

    fun markBillPaid(billId: Long, month: String, amount: Double, date: String) {
        viewModelScope.launch {
            val user = uiState.value.activeUser
            repository.insertBillPayment(
                BillPayment(
                    billId = billId,
                    month = month,
                    amount = amount,
                    paidDate = date,
                    addedBy = user
                )
            )
        }
    }

    fun deleteBillPayment(id: Long) {
        viewModelScope.launch {
            repository.deleteBillPayment(BillPayment(id = id, billId = 0, month = "", amount = 0.0, paidDate = "", addedBy = ""))
        }
    }

    fun addBill(name: String, nameBn: String, amount: Double, dueDay: Int) {
        viewModelScope.launch {
            repository.insertBill(
                Bill(
                    name = name,
                    nameBn = nameBn,
                    amount = amount,
                    dueDay = dueDay,
                    active = true
                )
            )
        }
    }

    fun toggleBillActive(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill.copy(active = !bill.active))
        }
    }

    fun addRefillItem(name: String, nameBn: String, category: String, unit: String, usualPrice: Double) {
        viewModelScope.launch {
            repository.insertRefillItem(
                RefillItem(
                    name = name,
                    nameBn = nameBn,
                    category = category,
                    unit = unit,
                    usualPrice = usualPrice
                )
            )
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val current = uiState.value
            repository.saveAppSettings(
                AppSettingsEntity(
                    language = lang,
                    activeUser = current.activeUser,
                    milkPricePerBottle = current.milkPricePerBottle,
                    useBengaliNumerals = current.useBengaliNumerals
                )
            )
        }
    }

    fun updateBengaliNumerals(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value
            repository.saveAppSettings(
                AppSettingsEntity(
                    language = current.language,
                    activeUser = current.activeUser,
                    milkPricePerBottle = current.milkPricePerBottle,
                    useBengaliNumerals = enabled
                )
            )
        }
    }

    fun setActiveUser(user: String) {
        viewModelScope.launch {
            val current = uiState.value
            repository.saveAppSettings(
                AppSettingsEntity(
                    language = current.language,
                    activeUser = user,
                    milkPricePerBottle = current.milkPricePerBottle,
                    useBengaliNumerals = current.useBengaliNumerals
                )
            )
        }
    }

    fun updateMilkPrice(price: Double) {
        viewModelScope.launch {
            val current = uiState.value
            repository.saveAppSettings(
                AppSettingsEntity(
                    language = current.language,
                    activeUser = current.activeUser,
                    milkPricePerBottle = price,
                    useBengaliNumerals = current.useBengaliNumerals
                )
            )
        }
    }

    fun updateHousehold(name: String, inviteCode: String, members: String) {
        viewModelScope.launch {
            repository.saveHousehold(
                HouseholdEntity(
                    name = name,
                    inviteCode = inviteCode,
                    members = members
                )
            )
        }
    }

    fun joinHousehold(code: String) {
        viewModelScope.launch {
            val current = uiState.value.household
            repository.saveHousehold(
                current.copy(inviteCode = code)
            )
        }
    }

    fun clearAllActivity() {
        viewModelScope.launch {
            repository.clearAllActivity()
        }
    }

    private fun daysBetween(startDateStr: String, endDateStr: String): Int {
        return try {
            val start = dateFormat.parse(startDateStr) ?: return 0
            val end = dateFormat.parse(endDateStr) ?: return 0
            val diffMillis = end.time - start.time
            TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
        } catch (_: Exception) {
            0
        }
    }

    private fun buildDueDateString(yearMonth: String, day: Int): String {
        val safeDay = when {
            day < 1 -> 1
            day > 28 -> 28 // safe for all months
            else -> day
        }
        val dayStr = if (safeDay < 10) "0$safeDay" else "$safeDay"
        return "$yearMonth-$dayStr"
    }
}

class GhorKhataViewModelFactory(
    private val repository: GhorKhataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GhorKhataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GhorKhataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
