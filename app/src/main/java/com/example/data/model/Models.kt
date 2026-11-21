package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage {
    EN, BN
}

enum class EntryType {
    REFILL,
    MILK_PICKUP,
    MILK_PAYMENT,
    BILL_PAYMENT,
    MILK_SKIP
}

@Entity(tableName = "refill_items")
data class RefillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameBn: String,
    val category: String,
    val unit: String,
    val usualPrice: Double
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val date: String, // format: "yyyy-MM-dd"
    val qty: Double,
    val price: Double,
    val addedBy: String,
    val note: String = ""
)

@Entity(tableName = "milk_pickups")
data class MilkPickup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format: "yyyy-MM-dd"
    val bottles: Int,
    val addedBy: String
)

@Entity(tableName = "milk_payments")
data class MilkPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format: "yyyy-MM-dd"
    val amount: Double,
    val addedBy: String,
    val note: String = ""
)

@Entity(tableName = "milk_skips")
data class MilkSkip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format: "yyyy-MM-dd"
    val note: String = ""
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameBn: String,
    val amount: Double,
    val dueDay: Int, // 1 to 31
    val active: Boolean = true
)

@Entity(tableName = "bill_payments")
data class BillPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val month: String, // format: "yyyy-MM"
    val amount: Double,
    val paidDate: String, // format: "yyyy-MM-dd"
    val addedBy: String
)

@Entity(tableName = "household")
data class HouseholdEntity(
    @PrimaryKey
    val id: String = "primary_household",
    val name: String,
    val inviteCode: String,
    val members: String // comma separated member names
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: String = "primary_settings",
    val language: String = "bn", // "en" or "bn"
    val activeUser: String = "Nadim",
    val milkPricePerBottle: Double = 90.0,
    val useBengaliNumerals: Boolean = true
)

// UI models for computed states
data class RefillItemStatus(
    val item: RefillItem,
    val lastPurchase: Purchase?,
    val daysSinceLastPurchase: Int?,
    val averageDaysBetweenPurchases: Int?,
    val runsOutInDays: Int?,
    val purchaseCount: Int
)

data class MilkMonthlySummary(
    val month: String, // "yyyy-MM"
    val totalBottles: Int,
    val pricePerBottle: Double,
    val currentCost: Double,
    val carryOverDue: Double,
    val paidThisMonth: Double,
    val netDue: Double,
    val daysSinceLastPickup: Int?
)

data class BillStatus(
    val bill: Bill,
    val isPaidThisMonth: Boolean,
    val payment: BillPayment?,
    val dueDueDateThisMonth: String, // "yyyy-MM-dd"
    val daysUntilDue: Int,
    val isOverdue: Boolean
)

data class CalendarDayEntry(
    val id: Long,
    val type: EntryType,
    val title: String,
    val subtitle: String,
    val amountOrQty: String,
    val addedBy: String,
    val date: String
)
