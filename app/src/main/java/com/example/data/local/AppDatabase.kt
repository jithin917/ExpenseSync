package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.PaymentSourceDao
import com.example.data.local.dao.PayeeDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        CategoryEntity::class,
        PaymentSourceEntity::class,
        PayeeEntity::class,
        TransactionEntity::class,
        ReceiptEntity::class,
        AppSettingEntity::class,
        AuditLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentSourceDao(): PaymentSourceDao
    abstract fun payeeDao(): PayeeDao
    abstract fun transactionDao(): TransactionDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 1 to 2 migrations if needed
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN defaultPaymentSourceId TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN defaultPaymentMode TEXT")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN defaultAmount REAL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN recurringFrequency TEXT")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN dueDayOfMonth INTEGER")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN recurringType TEXT")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN lastPaidDate INTEGER")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE payees ADD COLUMN isFlexibleSchedule INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expensesync_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val paymentSourceDao = database.paymentSourceDao()
            val payeeDao = database.payeeDao()
            val appSettingDao = database.appSettingDao()
            val transactionDao = database.transactionDao()
            val auditLogDao = database.auditLogDao()

            if (categoryDao.getCategoryCount() == 0) {
                val defaultCategories = listOf(
                    CategoryEntity(
                        id = "cat_lining",
                        name = "Lining & Materials",
                        iconName = "inventory",
                        colorHex = "#0D9488"
                    ),
                    CategoryEntity(
                        id = "cat_fabric",
                        name = "Fabric Purchases",
                        iconName = "inventory",
                        colorHex = "#0284C7"
                    ),
                    CategoryEntity(
                        id = "cat_accessories",
                        name = "Tailoring Accessories",
                        iconName = "inventory",
                        colorHex = "#7C3AED"
                    ),
                    CategoryEntity(
                        id = "cat_rent",
                        name = "Rent & Lease",
                        iconName = "hotel",
                        colorHex = "#EA580C"
                    ),
                    CategoryEntity(
                        id = "cat_salary",
                        name = "Salary & Wages",
                        iconName = "payments",
                        colorHex = "#16A34A"
                    ),
                    CategoryEntity(
                        id = "cat_utilities",
                        name = "Utilities & Electricity",
                        iconName = "bolt",
                        colorHex = "#CA8A04"
                    ),
                    CategoryEntity(
                        id = "cat_travel",
                        name = "Travel & Conveyance",
                        iconName = "flight",
                        colorHex = "#2563EB"
                    ),
                    CategoryEntity(
                        id = "cat_meals",
                        name = "Meals & Refreshments",
                        iconName = "restaurant",
                        colorHex = "#DB2777"
                    ),
                    CategoryEntity(
                        id = "cat_office",
                        name = "Office Supplies",
                        iconName = "inventory",
                        colorHex = "#059669"
                    ),
                    CategoryEntity(
                        id = "cat_misc",
                        name = "Miscellaneous",
                        iconName = "more_horiz",
                        colorHex = "#64748B"
                    )
                )
                categoryDao.insertCategories(defaultCategories)
            }

            if (paymentSourceDao.getPaymentSourceCount() == 0) {
                val defaultPaymentSources = listOf(
                    PaymentSourceEntity(
                        id = "src_hdfc_cc",
                        type = "CC",
                        bankName = "HDFC Bank",
                        cardName = "Corporate Visa",
                        last4Digits = "4892",
                        isDefault = true
                    ),
                    PaymentSourceEntity(
                        id = "src_gpay_upi",
                        type = "UPI",
                        bankName = "Google Pay (GPAY UPI)",
                        cardName = "GPAY",
                        accountNumber = "jithin@okaxis",
                        isDefault = false
                    ),
                    PaymentSourceEntity(
                        id = "src_cash",
                        type = "CASH",
                        bankName = "Cash / Petty Cash",
                        cardName = "Cash",
                        accountNumber = "CASH-DRAWER",
                        isDefault = false
                    ),
                    PaymentSourceEntity(
                        id = "src_sbi_sb",
                        type = "SB",
                        bankName = "State Bank of India",
                        accountNumber = "**** 5678",
                        ifscOrRouting = "SBIN0001234",
                        isDefault = false
                    ),
                    PaymentSourceEntity(
                        id = "src_icici_cc",
                        type = "CC",
                        bankName = "ICICI Bank",
                        cardName = "Platinum Business",
                        last4Digits = "9021",
                        isDefault = false
                    )
                )
                paymentSourceDao.insertPaymentSources(defaultPaymentSources)
            }

            if (payeeDao.getPayeeCount() == 0) {
                val defaultPayees = listOf(
                    PayeeEntity(
                        id = "payee_olive_fashion",
                        name = "Olive Fashion",
                        categoryTag = "Lining",
                        defaultCategoryId = "cat_lining",
                        defaultPaymentSourceId = "src_hdfc_cc",
                        defaultPaymentMode = "Credit Card (CC)",
                        hasGstBill = true,
                        gstNumber = "32ABCDE1234F1Z5",
                        phoneOrContact = "+91 98470 12345",
                        notes = "Selected vendor for premium lining materials (Default: Credit Card)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_radhakrishna",
                        name = "Radhakrishna Textiles",
                        categoryTag = "Fabric",
                        defaultCategoryId = "cat_fabric",
                        defaultPaymentSourceId = "src_hdfc_cc",
                        defaultPaymentMode = "Credit Card (CC)",
                        hasGstBill = true,
                        gstNumber = "32FGHIJ5678K1Z9",
                        phoneOrContact = "+91 98471 23456",
                        notes = "Major vendor for fabric and textiles (Default: Credit Card)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_parag_fashion",
                        name = "Parag Fashion",
                        categoryTag = "Fabric",
                        defaultCategoryId = "cat_fabric",
                        defaultPaymentSourceId = "src_hdfc_cc",
                        defaultPaymentMode = "Credit Card (CC)",
                        hasGstBill = true,
                        gstNumber = "32KLMNO9012P1Z3",
                        phoneOrContact = "+91 98472 34567",
                        notes = "Selected vendor for fabrics & textiles (Default: Credit Card)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_tailors_mart",
                        name = "Accessories Tailors Mart Aluva",
                        categoryTag = "Accessories",
                        defaultCategoryId = "cat_accessories",
                        defaultPaymentSourceId = "src_hdfc_cc",
                        defaultPaymentMode = "Credit Card (CC)",
                        hasGstBill = true,
                        gstNumber = "32PQRST3456U1Z7",
                        phoneOrContact = "+91 98473 45678",
                        notes = "Buttons, zippers, threads, and tailoring accessories in Aluva (Default: Credit Card)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_rent_landlord",
                        name = "Property Owner (Office Rent)",
                        categoryTag = "Rent",
                        defaultCategoryId = "cat_rent",
                        defaultPaymentSourceId = "src_gpay_upi",
                        defaultPaymentMode = "GPAY UPI / CASH",
                        defaultAmount = 15000.0,
                        isRecurring = true,
                        recurringFrequency = "Monthly",
                        dueDayOfMonth = 1,
                        recurringType = "RENT",
                        hasGstBill = false,
                        gstNumber = null,
                        phoneOrContact = "+91 98474 56789",
                        notes = "Default monthly office / workshop rent payee (Due 1st of month, GPAY UPI / Cash, ₹15,000/mo)",
                        isDefault = true
                    ),
                    PayeeEntity(
                        id = "payee_staff_salaries",
                        name = "Staff Payroll / Worker Wages",
                        categoryTag = "Salary",
                        defaultCategoryId = "cat_salary",
                        defaultPaymentSourceId = "src_sbi_sb",
                        defaultPaymentMode = "SB",
                        defaultAmount = 45000.0,
                        isRecurring = true,
                        recurringFrequency = "Monthly",
                        dueDayOfMonth = 5,
                        recurringType = "SALARY",
                        hasGstBill = false,
                        gstNumber = null,
                        phoneOrContact = "+91 98475 67890",
                        notes = "Monthly staff salaries & tailoring worker disbursements (Due 5th of month, ₹45,000/mo)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_electricity_bill",
                        name = "Electricity Board (KSEB Power)",
                        categoryTag = "Electricity",
                        defaultCategoryId = "cat_utilities",
                        defaultPaymentSourceId = "src_gpay_upi",
                        defaultPaymentMode = "GPAY UPI / CASH",
                        defaultAmount = 3800.0,
                        isRecurring = true,
                        recurringFrequency = "Monthly",
                        dueDayOfMonth = 10,
                        recurringType = "UTILITY",
                        hasGstBill = true,
                        gstNumber = "32AAAAA0000A1Z5",
                        phoneOrContact = "1912",
                        notes = "Workshop electricity & industrial power bill (Due 10th of month, ₹3,800/mo)",
                        isDefault = false
                    ),
                    PayeeEntity(
                        id = "payee_airtel_fiber",
                        name = "Airtel Commercial Fiber",
                        categoryTag = "Internet",
                        defaultCategoryId = "cat_office",
                        defaultPaymentSourceId = "src_hdfc_cc",
                        defaultPaymentMode = "Credit Card (CC)",
                        defaultAmount = 1199.0,
                        isRecurring = true,
                        recurringFrequency = "Monthly",
                        dueDayOfMonth = 15,
                        recurringType = "SUBSCRIPTION",
                        hasGstBill = true,
                        gstNumber = "32AABCA1234F1Z1",
                        phoneOrContact = "121",
                        notes = "High-speed broadband & office internet (Due 15th of month, ₹1,199/mo)",
                        isDefault = false
                    )
                )
                payeeDao.insertPayees(defaultPayees)
            }

            val defaultSettings = listOf(
                AppSettingEntity("employee_name", "Jithin Kumar"),
                AppSettingEntity("employee_id", "EMP-204"),
                AppSettingEntity("company_name", "Acme Corporation"),
                AppSettingEntity("accountant_email", "accounts@acmecorp.com"),
                AppSettingEntity("email_frequency", "Daily batch"),
                AppSettingEntity("currency_symbol", "₹"),
                AppSettingEntity("auto_sync_enabled", "true"),
                AppSettingEntity("projection_lookahead_days", "10"),
                AppSettingEntity("recurring_notifications_enabled", "true"),
                AppSettingEntity("recurring_alert_threshold_days", "3")
            )
            appSettingDao.saveSettings(defaultSettings)

            // Seed sample initial transactions with Payees & GST info
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val txn1 = TransactionEntity(
                id = "txn_sample_1",
                amount = 4500.00,
                transactionDate = now - (dayMs * 1),
                categoryId = "cat_lining",
                sourceId = "src_hdfc_cc",
                payeeId = "payee_olive_fashion",
                notes = "Premium cotton lining materials batch purchase",
                merchantName = "Olive Fashion",
                hasGstBill = true,
                gstNumber = "32ABCDE1234F1Z5",
                voucherType = "Payment",
                syncStatus = "PENDING"
            )
            val txn2 = TransactionEntity(
                id = "txn_sample_2",
                amount = 12500.00,
                transactionDate = now - (dayMs * 2),
                categoryId = "cat_fabric",
                sourceId = "src_sbi_sb",
                payeeId = "payee_radhakrishna",
                notes = "Rolls of silk and blended fabric for manufacturing",
                merchantName = "Radhakrishna Textiles",
                hasGstBill = true,
                gstNumber = "32FGHIJ5678K1Z9",
                voucherType = "Payment",
                syncStatus = "PENDING"
            )
            val txn3 = TransactionEntity(
                id = "txn_sample_3",
                amount = 1800.00,
                transactionDate = now - (dayMs * 3),
                categoryId = "cat_accessories",
                sourceId = "src_hdfc_cc",
                payeeId = "payee_tailors_mart",
                notes = "Zippers, buttons, elastic cords & tailoring accessories",
                merchantName = "Accessories Tailors Mart Aluva",
                hasGstBill = true,
                gstNumber = "32PQRST3456U1Z7",
                voucherType = "Payment",
                syncStatus = "PENDING"
            )
            transactionDao.insertTransaction(txn1)
            transactionDao.insertTransaction(txn2)
            transactionDao.insertTransaction(txn3)

            auditLogDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = txn1.id,
                    action = "CREATED",
                    details = "Purchase from Olive Fashion logged (GST: 32ABCDE1234F1Z5)",
                    timestamp = txn1.createdAt
                )
            )
            auditLogDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = txn2.id,
                    action = "CREATED",
                    details = "Purchase from Radhakrishna Textiles logged (GST: 32FGHIJ5678K1Z9)",
                    timestamp = txn2.createdAt
                )
            )
            auditLogDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = txn3.id,
                    action = "CREATED",
                    details = "Purchase from Accessories Tailors Mart Aluva logged (GST: 32PQRST3456U1Z7)",
                    timestamp = txn3.createdAt
                )
            )
        }
    }
}
