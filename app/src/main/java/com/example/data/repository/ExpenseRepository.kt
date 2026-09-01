package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.export.EmailReportPackage
import com.example.data.export.EmailReportService
import com.example.data.export.TallyExportResult
import com.example.data.export.TallyExportService
import com.example.data.image.ImageProcessor
import com.example.data.image.ProcessedImageResult
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionWithDetails
import com.example.data.sync.FirebaseSyncService
import com.example.data.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ExpenseRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val categoryDao = database.categoryDao()
    private val paymentSourceDao = database.paymentSourceDao()
    private val payeeDao = database.payeeDao()
    private val transactionDao = database.transactionDao()
    private val receiptDao = database.receiptDao()
    private val appSettingDao = database.appSettingDao()
    private val auditLogDao = database.auditLogDao()

    private val imageProcessor = ImageProcessor(context)
    private val tallyExportService = TallyExportService(context)
    private val emailReportService = EmailReportService(context)
    private val firebaseSyncService = FirebaseSyncService(context)

    // Reactive streams
    val allTransactions: Flow<List<TransactionWithDetails>> = transactionDao.getAllTransactionsWithDetails()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
    val allPaymentSources: Flow<List<PaymentSourceEntity>> = paymentSourceDao.getAllPaymentSources()
    val allPayees: Flow<List<PayeeEntity>> = payeeDao.getAllPayees()
    val pendingSyncCount: Flow<Int> = transactionDao.getPendingSyncCount()
    val allSettings: Flow<List<AppSettingEntity>> = appSettingDao.getAllSettings()
    val auditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    suspend fun getTransactionById(id: String): TransactionWithDetails? {
        return transactionDao.getTransactionWithDetailsById(id)
    }

    fun getSettingFlow(key: String): Flow<String?> = appSettingDao.getSettingFlow(key)

    suspend fun getSetting(key: String, defaultValue: String = ""): String {
        return appSettingDao.getSetting(key) ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.saveSetting(AppSettingEntity(key, value))
    }

    suspend fun saveSettings(settings: Map<String, String>) {
        appSettingDao.saveSettings(settings.map { AppSettingEntity(it.key, it.value) })
    }

    /**
     * Creates or updates an Expense Transaction, records audit trail, and attaches receipt.
     */
    suspend fun saveTransaction(
        id: String? = null,
        amount: Double,
        transactionDate: Long,
        categoryId: String?,
        sourceId: String?,
        payeeId: String? = null,
        notes: String?,
        merchantName: String?,
        hasGstBill: Boolean = false,
        gstNumber: String? = null,
        gstRate: Double? = null,
        voucherType: String = "Payment",
        receiptImageUri: Uri? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val isNew = id.isNullOrBlank()
            val txnId = if (isNew) "txn_${UUID.randomUUID()}" else id!!

            val transactionEntity = TransactionEntity(
                id = txnId,
                amount = amount,
                transactionDate = transactionDate,
                categoryId = categoryId,
                sourceId = sourceId,
                payeeId = payeeId,
                notes = notes?.trim(),
                merchantName = merchantName?.trim(),
                hasGstBill = hasGstBill,
                gstNumber = gstNumber?.trim(),
                gstRate = gstRate,
                voucherType = voucherType,
                syncStatus = "PENDING",
                createdAt = if (isNew) System.currentTimeMillis() else (transactionDao.getTransactionById(txnId)?.createdAt ?: System.currentTimeMillis()),
                updatedAt = System.currentTimeMillis()
            )

            transactionDao.insertTransaction(transactionEntity)

            // Handle Receipt Image if provided
            if (receiptImageUri != null) {
                when (val processResult = imageProcessor.processAndSaveImage(receiptImageUri)) {
                    is ProcessedImageResult.Success -> {
                        val receiptEntity = ReceiptEntity(
                            id = "rcpt_${UUID.randomUUID()}",
                            transactionId = txnId,
                            localFilePath = processResult.filePath,
                            fileSizeKb = processResult.fileSizeKb,
                            syncStatus = "PENDING"
                        )
                        receiptDao.insertReceipt(receiptEntity)
                    }
                    is ProcessedImageResult.Error -> {
                        // Log or proceed without failing whole transaction
                    }
                }
            }

            // Record Audit Trail
            val action = if (isNew) "CREATED" else "UPDATED"
            val payeeDisplay = merchantName ?: "No Payee"
            val gstDisplay = if (hasGstBill && !gstNumber.isNullOrBlank()) " [GST: $gstNumber]" else ""
            val auditNote = if (isNew) {
                "Logged new transaction of $amount to $payeeDisplay$gstDisplay"
            } else {
                "Updated transaction details (Amount: $amount, Payee: $payeeDisplay)"
            }

            auditLogDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = txnId,
                    action = action,
                    details = auditNote,
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(txnId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete local receipt files
            val receipts = receiptDao.getReceiptsForTransactionOnce(id)
            for (r in receipts) {
                val f = File(r.localFilePath)
                if (f.exists()) f.delete()
            }
            receiptDao.deleteReceiptsForTransaction(id)
            transactionDao.deleteTransaction(id)

            auditLogDao.insertLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = id,
                    action = "DELETED",
                    details = "Transaction deleted from local records",
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Category Management
    suspend fun saveCategory(
        id: String? = null,
        name: String,
        parentId: String? = null,
        iconName: String = "category",
        colorHex: String = "#0284C7"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val catId = id ?: "cat_${UUID.randomUUID()}"
            val entity = CategoryEntity(
                id = catId,
                name = name.trim(),
                parentId = parentId,
                iconName = iconName,
                colorHex = colorHex,
                isActive = true,
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
            categoryDao.insertCategory(entity)
            Result.success(catId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            categoryDao.deleteCategory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Payment Source Management
    suspend fun savePaymentSource(
        id: String? = null,
        type: String, // "SB" or "CC"
        bankName: String,
        cardName: String? = null,
        accountNumber: String? = null,
        ifscOrRouting: String? = null,
        last4Digits: String? = null,
        isDefault: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val srcId = id ?: "src_${UUID.randomUUID()}"
            val entity = PaymentSourceEntity(
                id = srcId,
                type = type,
                bankName = bankName.trim(),
                cardName = cardName?.trim(),
                accountNumber = accountNumber?.trim(),
                ifscOrRouting = ifscOrRouting?.trim(),
                last4Digits = last4Digits?.trim(),
                isDefault = isDefault,
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
            paymentSourceDao.insertPaymentSource(entity)
            if (isDefault) {
                paymentSourceDao.setDefaultPaymentSource(srcId)
            }
            Result.success(srcId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePaymentSource(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            paymentSourceDao.deletePaymentSource(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultPaymentSource(id: String) = withContext(Dispatchers.IO) {
        paymentSourceDao.setDefaultPaymentSource(id)
    }

    // Payee / Vendor Management
    suspend fun savePayee(
        id: String? = null,
        name: String,
        categoryTag: String? = null,
        defaultCategoryId: String? = null,
        defaultPaymentSourceId: String? = null,
        defaultPaymentMode: String? = null,
        defaultAmount: Double? = null,
        isRecurring: Boolean = false,
        recurringFrequency: String? = null,
        dueDayOfMonth: Int? = null,
        recurringType: String? = null,
        isFlexibleSchedule: Boolean = false,
        lastPaidDate: Long? = null,
        hasGstBill: Boolean = false,
        gstNumber: String? = null,
        phoneOrContact: String? = null,
        notes: String? = null,
        isDefault: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payeeId = id ?: "payee_${UUID.randomUUID()}"
            val entity = PayeeEntity(
                id = payeeId,
                name = name.trim(),
                categoryTag = categoryTag?.trim()?.ifBlank { null },
                defaultCategoryId = defaultCategoryId?.ifBlank { null },
                defaultPaymentSourceId = defaultPaymentSourceId?.ifBlank { null },
                defaultPaymentMode = defaultPaymentMode?.trim()?.ifBlank { null },
                defaultAmount = defaultAmount,
                isRecurring = isRecurring,
                recurringFrequency = recurringFrequency?.trim()?.ifBlank { null },
                dueDayOfMonth = dueDayOfMonth,
                recurringType = recurringType?.trim()?.ifBlank { null },
                isFlexibleSchedule = isFlexibleSchedule,
                lastPaidDate = lastPaidDate,
                hasGstBill = hasGstBill,
                gstNumber = gstNumber?.trim()?.ifBlank { null },
                phoneOrContact = phoneOrContact?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                isDefault = isDefault,
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
            payeeDao.insertPayee(entity)
            if (isDefault) {
                payeeDao.setDefaultPayee(payeeId)
            }
            Result.success(payeeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePayee(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            payeeDao.deletePayee(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultPayee(id: String) = withContext(Dispatchers.IO) {
        payeeDao.setDefaultPayee(id)
    }

    // Image helper
    fun createTempCameraUri(): Uri = imageProcessor.createTempCameraUri()

    // Export & Email
    suspend fun exportTallyCsv(transactions: List<TransactionWithDetails>): TallyExportResult {
        val employeeName = getSetting("employee_name", "Employee")
        return tallyExportService.generateTallyCsv(transactions, employeeName)
    }

    suspend fun exportTallyXml(transactions: List<TransactionWithDetails>): TallyExportResult {
        val companyName = getSetting("company_name", "Acme Corporation")
        return tallyExportService.generateTallyXml(transactions, companyName)
    }

    suspend fun generateEmailReport(
        transactions: List<TransactionWithDetails>,
        dateRangeLabel: String
    ): EmailReportPackage = withContext(Dispatchers.IO) {
        val empName = getSetting("employee_name", "Employee")
        val empId = getSetting("employee_id", "EMP-101")
        val company = getSetting("company_name", "Company")
        val accountantEmail = getSetting("accountant_email", "accounts@company.com")
        val currencySymbol = getSetting("currency_symbol", "₹")

        // First generate CSV to attach
        val csvResult = tallyExportService.generateTallyCsv(transactions, empName)
        val csvFile = if (csvResult is TallyExportResult.Success) csvResult.file else null

        emailReportService.createEmailReport(
            transactions = transactions,
            employeeName = empName,
            employeeId = empId,
            companyName = company,
            accountantEmail = accountantEmail,
            currencySymbol = currencySymbol,
            dateRangeLabel = dateRangeLabel,
            csvFile = csvFile
        )
    }

    // Cloud & Sync
    suspend fun syncAllToCloud(): SyncResult = withContext(Dispatchers.IO) {
        val txns = allTransactions.firstOrNull() ?: emptyList()
        val cats = allCategories.firstOrNull() ?: emptyList()
        val srcs = allPaymentSources.firstOrNull() ?: emptyList()
        val empId = getSetting("employee_id", "EMP-204")

        val result = firebaseSyncService.syncTransactionsToCloud(
            userId = empId,
            transactions = txns,
            categories = cats,
            paymentSources = srcs
        )

        if (result is SyncResult.Success) {
            transactionDao.markAllAsSynced()
        }

        result
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        AppDatabase.populateInitialData(database)
    }
}
