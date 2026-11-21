package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GhorKhataDao {

    // Refill Items
    @Query("SELECT * FROM refill_items ORDER BY id ASC")
    fun getAllRefillItems(): Flow<List<RefillItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefillItem(item: RefillItem): Long

    @Update
    suspend fun updateRefillItem(item: RefillItem)

    @Delete
    suspend fun deleteRefillItem(item: RefillItem)

    // Purchases
    @Query("SELECT * FROM purchases ORDER BY date DESC, id DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE itemId = :itemId ORDER BY date DESC, id DESC")
    fun getPurchasesForItem(itemId: Long): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Delete
    suspend fun deletePurchase(purchase: Purchase)

    // Milk Pickups
    @Query("SELECT * FROM milk_pickups ORDER BY date DESC, id DESC")
    fun getAllMilkPickups(): Flow<List<MilkPickup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkPickup(pickup: MilkPickup): Long

    @Delete
    suspend fun deleteMilkPickup(pickup: MilkPickup)

    // Milk Payments
    @Query("SELECT * FROM milk_payments ORDER BY date DESC, id DESC")
    fun getAllMilkPayments(): Flow<List<MilkPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkPayment(payment: MilkPayment): Long

    @Delete
    suspend fun deleteMilkPayment(payment: MilkPayment)

    // Milk Skips
    @Query("SELECT * FROM milk_skips ORDER BY date DESC")
    fun getAllMilkSkips(): Flow<List<MilkSkip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilkSkip(skip: MilkSkip): Long

    @Delete
    suspend fun deleteMilkSkip(skip: MilkSkip)

    // Bills
    @Query("SELECT * FROM bills ORDER BY dueDay ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    // Bill Payments
    @Query("SELECT * FROM bill_payments ORDER BY paidDate DESC")
    fun getAllBillPayments(): Flow<List<BillPayment>>

    @Query("SELECT * FROM bill_payments WHERE month = :month")
    fun getBillPaymentsForMonth(month: String): Flow<List<BillPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillPayment(payment: BillPayment): Long

    @Delete
    suspend fun deleteBillPayment(payment: BillPayment)

    // Household & Settings
    @Query("SELECT * FROM household WHERE id = 'primary_household'")
    fun getHousehold(): Flow<HouseholdEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setHousehold(household: HouseholdEntity)

    @Query("SELECT * FROM app_settings WHERE id = 'primary_settings'")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAppSettings(settings: AppSettingsEntity)

    @Query("DELETE FROM purchases")
    suspend fun clearAllPurchases()

    @Query("DELETE FROM milk_pickups")
    suspend fun clearAllMilkPickups()

    @Query("DELETE FROM milk_payments")
    suspend fun clearAllMilkPayments()

    @Query("DELETE FROM milk_skips")
    suspend fun clearAllMilkSkips()

    @Query("DELETE FROM bill_payments")
    suspend fun clearAllBillPayments()

    @Query("DELETE FROM bills")
    suspend fun clearAllBills()
}
