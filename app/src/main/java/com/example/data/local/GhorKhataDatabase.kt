package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RefillItem::class,
        Purchase::class,
        MilkPickup::class,
        MilkPayment::class,
        MilkSkip::class,
        Bill::class,
        BillPayment::class,
        HouseholdEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GhorKhataDatabase : RoomDatabase() {

    abstract fun ghorKhataDao(): GhorKhataDao

    companion object {
        @Volatile
        private var INSTANCE: GhorKhataDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GhorKhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GhorKhataDatabase::class.java,
                    "ghor_khata_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateCleanInitialData(database.ghorKhataDao())
                    }
                }
            }

            private suspend fun populateCleanInitialData(dao: GhorKhataDao) {
                // 1. Minimal Default Household & Settings (no dummy users or activity)
                dao.setHousehold(
                    HouseholdEntity(
                        id = "primary_household",
                        name = "আমাদের পরিবার",
                        inviteCode = "GK-${(1000..9999).random()}",
                        members = "আমি"
                    )
                )

                dao.setAppSettings(
                    AppSettingsEntity(
                        id = "primary_settings",
                        language = "bn",
                        activeUser = "আমি",
                        milkPricePerBottle = 90.0,
                        useBengaliNumerals = true
                    )
                )

                // 2. Only clean default refill items without any fake purchases
                dao.insertRefillItem(
                    RefillItem(
                        name = "Gas Cylinder",
                        nameBn = "গ্যাস সিলিন্ডার",
                        category = "Kitchen",
                        unit = "12kg",
                        usualPrice = 1400.0
                    )
                )
                dao.insertRefillItem(
                    RefillItem(
                        name = "Rice",
                        nameBn = "চাল",
                        category = "Groceries",
                        unit = "25kg",
                        usualPrice = 1800.0
                    )
                )
                dao.insertRefillItem(
                    RefillItem(
                        name = "Cooking Oil",
                        nameBn = "তেল",
                        category = "Groceries",
                        unit = "5L",
                        usualPrice = 850.0
                    )
                )

                // 0 dummy purchases
                // 0 dummy milk pickups
                // 0 dummy milk payments
                // 0 dummy milk skips
                // 0 dummy bill payments
            }
        }
    }
}
