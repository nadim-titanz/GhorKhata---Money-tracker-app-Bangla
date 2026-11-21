package com.example.data.repository

import com.example.data.local.GhorKhataDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class GhorKhataRepository(private val dao: GhorKhataDao) {

    val allRefillItems: Flow<List<RefillItem>> = dao.getAllRefillItems()
    val allPurchases: Flow<List<Purchase>> = dao.getAllPurchases()
    val allMilkPickups: Flow<List<MilkPickup>> = dao.getAllMilkPickups()
    val allMilkPayments: Flow<List<MilkPayment>> = dao.getAllMilkPayments()
    val allMilkSkips: Flow<List<MilkSkip>> = dao.getAllMilkSkips()
    val allBills: Flow<List<Bill>> = dao.getAllBills()
    val allBillPayments: Flow<List<BillPayment>> = dao.getAllBillPayments()
    val household: Flow<HouseholdEntity?> = dao.getHousehold()
    val appSettings: Flow<AppSettingsEntity?> = dao.getAppSettings()

    fun getBillPaymentsForMonth(month: String): Flow<List<BillPayment>> =
        dao.getBillPaymentsForMonth(month)

    suspend fun insertRefillItem(item: RefillItem): Long = dao.insertRefillItem(item)
    suspend fun updateRefillItem(item: RefillItem) = dao.updateRefillItem(item)
    suspend fun deleteRefillItem(item: RefillItem) = dao.deleteRefillItem(item)

    suspend fun insertPurchase(purchase: Purchase): Long = dao.insertPurchase(purchase)
    suspend fun deletePurchase(purchase: Purchase) = dao.deletePurchase(purchase)

    suspend fun insertMilkPickup(pickup: MilkPickup): Long = dao.insertMilkPickup(pickup)
    suspend fun deleteMilkPickup(pickup: MilkPickup) = dao.deleteMilkPickup(pickup)

    suspend fun insertMilkPayment(payment: MilkPayment): Long = dao.insertMilkPayment(payment)
    suspend fun deleteMilkPayment(payment: MilkPayment) = dao.deleteMilkPayment(payment)

    suspend fun insertMilkSkip(skip: MilkSkip): Long = dao.insertMilkSkip(skip)
    suspend fun deleteMilkSkip(skip: MilkSkip) = dao.deleteMilkSkip(skip)

    suspend fun insertBill(bill: Bill): Long = dao.insertBill(bill)
    suspend fun updateBill(bill: Bill) = dao.updateBill(bill)
    suspend fun deleteBill(bill: Bill) = dao.deleteBill(bill)

    suspend fun insertBillPayment(payment: BillPayment): Long = dao.insertBillPayment(payment)
    suspend fun deleteBillPayment(payment: BillPayment) = dao.deleteBillPayment(payment)

    suspend fun saveHousehold(household: HouseholdEntity) = dao.setHousehold(household)
    suspend fun saveAppSettings(settings: AppSettingsEntity) = dao.setAppSettings(settings)

    suspend fun clearAllActivity() {
        dao.clearAllPurchases()
        dao.clearAllMilkPickups()
        dao.clearAllMilkPayments()
        dao.clearAllMilkSkips()
        dao.clearAllBillPayments()
        dao.clearAllBills()
    }
}
