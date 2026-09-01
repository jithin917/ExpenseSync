package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.EmailReportPackage
import com.example.data.export.TallyExportResult
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionWithDetails
import com.example.data.model.CashProjectionEngine
import com.example.data.model.CashProjectionSummary
import com.example.data.model.RecurringDebitType
import com.example.data.model.UpcomingDebitItem
import com.example.data.notification.RecurringDebitNotificationHelper
import com.example.data.repository.ExpenseRepository
import com.example.data.sync.SyncResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DateFilterPreset(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Range")
}

data class DashboardUiState(
    val transactions: List<TransactionWithDetails> = emptyList(),
    val filteredTransactions: List<TransactionWithDetails> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val paymentSources: List<PaymentSourceEntity> = emptyList(),
    val payees: List<PayeeEntity> = emptyList(),
    val settingsMap: Map<String, String> = emptyMap(),
    val cashProjection: CashProjectionSummary = CashProjectionSummary(),
    val pendingSyncCount: Int = 0,
    val totalToday: Double = 0.0,
    val totalThisMonth: Double = 0.0,
    val totalFiltered: Double = 0.0,
    val selectedCategoryFilter: String? = null,
    val selectedSourceFilter: String? = null,
    val selectedPayeeFilter: String? = null,
    val selectedDatePreset: DateFilterPreset = DateFilterPreset.THIS_MONTH,
    val customStartDate: Long = 0L,
    val customEndDate: Long = 0L,
    val searchQuery: String = "",
    val isSyncing: Boolean = false
)

data class ExpenseFormState(
    val id: String? = null,
    val amountText: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val categoryId: String? = null,
    val sourceId: String? = null,
    val payeeId: String? = null,
    val merchantName: String = "",
    val hasGstBill: Boolean = false,
    val gstNumber: String = "",
    val gstRate: Double? = null,
    val notes: String = "",
    val receiptImageUri: Uri? = null,
    val existingReceiptUrl: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application)
    private val notificationHelper = RecurringDebitNotificationHelper(application)

    // Filter controls
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedSourceFilter = MutableStateFlow<String?>(null)
    val selectedSourceFilter: StateFlow<String?> = _selectedSourceFilter.asStateFlow()

    private val _selectedPayeeFilter = MutableStateFlow<String?>(null)
    val selectedPayeeFilter: StateFlow<String?> = _selectedPayeeFilter.asStateFlow()

    private val _selectedDatePreset = MutableStateFlow(DateFilterPreset.THIS_MONTH)
    val selectedDatePreset: StateFlow<DateFilterPreset> = _selectedDatePreset.asStateFlow()

    private val _customStartDate = MutableStateFlow(getStartOfMonthTimestamp())
    private val _customEndDate = MutableStateFlow(getEndOfDayTimestamp())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Form state
    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    // Single-event notification stream
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentSources: StateFlow<List<PaymentSourceEntity>> = repository.allPaymentSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payees: StateFlow<List<PayeeEntity>> = repository.allPayees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val appSettings: StateFlow<List<AppSettingEntity>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        repository.allTransactions,
        repository.allCategories,
        repository.allPaymentSources,
        repository.allPayees,
        repository.allSettings,
        repository.pendingSyncCount,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedSourceFilter,
        _selectedPayeeFilter,
        _selectedDatePreset,
        _customStartDate,
        _customEndDate,
        _isSyncing
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val allTxns = args[0] as List<TransactionWithDetails>
        @Suppress("UNCHECKED_CAST")
        val cats = args[1] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val srcs = args[2] as List<PaymentSourceEntity>
        @Suppress("UNCHECKED_CAST")
        val payeeList = args[3] as List<PayeeEntity>
        @Suppress("UNCHECKED_CAST")
        val settings = args[4] as List<AppSettingEntity>
        val pendingCount = args[5] as Int
        val search = args[6] as String
        val catFilter = args[7] as String?
        val srcFilter = args[8] as String?
        val payeeFilter = args[9] as String?
        val datePreset = args[10] as DateFilterPreset
        val customStart = args[11] as Long
        val customEnd = args[12] as Long
        val syncing = args[13] as Boolean

        val settingsMap = settings.associate { it.key to it.value }

        // Projections & Recurring Debits calculation
        val lookaheadDays = settingsMap["projection_lookahead_days"]?.toIntOrNull() ?: 10
        val notifsEnabled = settingsMap["recurring_notifications_enabled"]?.toBooleanStrictOrNull() ?: true
        val alertThreshold = settingsMap["recurring_alert_threshold_days"]?.toIntOrNull() ?: 3

        val cashProjection = CashProjectionEngine.calculateProjection(
            payees = payeeList,
            categories = cats,
            transactions = allTxns,
            lookaheadDays = lookaheadDays,
            notificationsEnabled = notifsEnabled,
            alertThresholdDays = alertThreshold
        )

        // Date bounds calculation
        val (startDate, endDate) = when (datePreset) {
            DateFilterPreset.ALL -> Pair(0L, Long.MAX_VALUE)
            DateFilterPreset.TODAY -> Pair(getStartOfDayTimestamp(), getEndOfDayTimestamp())
            DateFilterPreset.THIS_WEEK -> Pair(getStartOfWeekTimestamp(), getEndOfDayTimestamp())
            DateFilterPreset.THIS_MONTH -> Pair(getStartOfMonthTimestamp(), getEndOfDayTimestamp())
            DateFilterPreset.CUSTOM -> Pair(customStart, customEnd)
        }

        // Totals
        val todayStart = getStartOfDayTimestamp()
        val todayEnd = getEndOfDayTimestamp()
        val monthStart = getStartOfMonthTimestamp()

        val totalToday = allTxns.filter { it.transaction.transactionDate in todayStart..todayEnd }
            .sumOf { it.transaction.amount }

        val totalThisMonth = allTxns.filter { it.transaction.transactionDate in monthStart..todayEnd }
            .sumOf { it.transaction.amount }

        // Filter items
        val filtered = allTxns.filter { item ->
            val matchesDate = item.transaction.transactionDate in startDate..endDate
            val matchesCat = catFilter == null || item.transaction.categoryId == catFilter
            val matchesSrc = srcFilter == null || item.transaction.sourceId == srcFilter
            val matchesPayee = payeeFilter == null || item.transaction.payeeId == payeeFilter
            val matchesSearch = search.isBlank() ||
                (item.category?.name?.contains(search, ignoreCase = true) == true) ||
                (item.transaction.merchantName?.contains(search, ignoreCase = true) == true) ||
                (item.payee?.name?.contains(search, ignoreCase = true) == true) ||
                (item.payee?.categoryTag?.contains(search, ignoreCase = true) == true) ||
                (item.transaction.gstNumber?.contains(search, ignoreCase = true) == true) ||
                (item.transaction.notes?.contains(search, ignoreCase = true) == true) ||
                (item.paymentSource?.displayLabel?.contains(search, ignoreCase = true) == true)

            matchesDate && matchesCat && matchesSrc && matchesPayee && matchesSearch
        }

        val totalFiltered = filtered.sumOf { it.transaction.amount }

        DashboardUiState(
            transactions = allTxns,
            filteredTransactions = filtered,
            categories = cats,
            paymentSources = srcs,
            payees = payeeList,
            settingsMap = settingsMap,
            cashProjection = cashProjection,
            pendingSyncCount = pendingCount,
            totalToday = totalToday,
            totalThisMonth = totalThisMonth,
            totalFiltered = totalFiltered,
            selectedCategoryFilter = catFilter,
            selectedSourceFilter = srcFilter,
            selectedPayeeFilter = payeeFilter,
            selectedDatePreset = datePreset,
            customStartDate = customStart,
            customEndDate = customEnd,
            searchQuery = search,
            isSyncing = syncing
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setSourceFilter(sourceId: String?) {
        _selectedSourceFilter.value = sourceId
    }

    fun setPayeeFilter(payeeId: String?) {
        _selectedPayeeFilter.value = payeeId
    }

    fun setDatePreset(preset: DateFilterPreset) {
        _selectedDatePreset.value = preset
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedDatePreset.value = DateFilterPreset.CUSTOM
    }

    // Form handlers
    fun initAddForm(initialImageUri: Uri? = null) {
        val defaultSrc = paymentSources.value.firstOrNull { it.isDefault } ?: paymentSources.value.firstOrNull()
        val defaultCat = categories.value.firstOrNull()
        _formState.value = ExpenseFormState(
            transactionDate = System.currentTimeMillis(),
            categoryId = defaultCat?.id,
            sourceId = defaultSrc?.id,
            receiptImageUri = initialImageUri
        )
    }

    fun initEditForm(transactionWithDetails: TransactionWithDetails) {
        val txn = transactionWithDetails.transaction
        val firstReceipt = transactionWithDetails.receipts.firstOrNull()
        val associatedPayee = transactionWithDetails.payee
        _formState.value = ExpenseFormState(
            id = txn.id,
            amountText = if (txn.amount > 0) txn.amount.toString() else "",
            transactionDate = txn.transactionDate,
            categoryId = txn.categoryId,
            sourceId = txn.sourceId,
            payeeId = txn.payeeId,
            merchantName = txn.merchantName ?: associatedPayee?.name ?: "",
            hasGstBill = txn.hasGstBill || (associatedPayee?.hasGstBill == true),
            gstNumber = txn.gstNumber ?: associatedPayee?.gstNumber ?: "",
            gstRate = txn.gstRate,
            notes = txn.notes ?: "",
            existingReceiptUrl = firstReceipt?.localFilePath
        )
    }

    fun updateFormAmount(amount: String) {
        // Only allow numbers and one decimal point
        if (amount.isEmpty() || amount.matches("^\\d*\\.?\\d{0,2}$".toRegex())) {
            _formState.value = _formState.value.copy(amountText = amount, errorMessage = null)
        }
    }

    fun updateFormDate(dateMs: Long) {
        _formState.value = _formState.value.copy(transactionDate = dateMs)
    }

    fun updateFormCategory(catId: String) {
        val allSources = paymentSources.value
        val newSourceId = when (catId) {
            "cat_rent" -> {
                // For rent, prefer GPAY UPI / Cash
                allSources.firstOrNull {
                    it.type.equals("UPI", ignoreCase = true) ||
                    it.type.equals("CASH", ignoreCase = true) ||
                    it.bankName.contains("UPI", ignoreCase = true) ||
                    it.bankName.contains("Cash", ignoreCase = true) ||
                    it.bankName.contains("GPAY", ignoreCase = true)
                }?.id ?: _formState.value.sourceId
            }
            "cat_fabric", "cat_lining", "cat_accessories" -> {
                // For fabric and lining purchases, prefer Credit Card
                allSources.firstOrNull { it.type.equals("CC", ignoreCase = true) }?.id ?: _formState.value.sourceId
            }
            else -> _formState.value.sourceId
        }
        _formState.value = _formState.value.copy(
            categoryId = catId,
            sourceId = newSourceId
        )
    }

    fun updateFormSource(sourceId: String) {
        _formState.value = _formState.value.copy(sourceId = sourceId)
    }

    fun selectPayee(payee: PayeeEntity?) {
        if (payee != null) {
            val allSources = paymentSources.value
            val matchedSourceId = payee.defaultPaymentSourceId?.takeIf { sourceId ->
                allSources.any { it.id == sourceId }
            } ?: run {
                val mode = (payee.defaultPaymentMode ?: "").uppercase()
                val tag = (payee.categoryTag ?: "").uppercase()
                val name = payee.name.uppercase()
                when {
                    mode.contains("CC") || mode.contains("CREDIT") || tag.contains("FABRIC") || tag.contains("LINING") || tag.contains("ACCESSORIES") -> {
                        allSources.firstOrNull { it.type.equals("CC", ignoreCase = true) }?.id
                    }
                    mode.contains("UPI") || mode.contains("GPAY") || mode.contains("CASH") || tag.contains("RENT") || name.contains("RENT") -> {
                        allSources.firstOrNull {
                            it.type.equals("UPI", ignoreCase = true) ||
                            it.type.equals("CASH", ignoreCase = true) ||
                            it.bankName.contains("UPI", ignoreCase = true) ||
                            it.bankName.contains("Cash", ignoreCase = true) ||
                            it.bankName.contains("GPAY", ignoreCase = true)
                        }?.id
                    }
                    mode.contains("SB") -> {
                        allSources.firstOrNull { it.type.equals("SB", ignoreCase = true) }?.id
                    }
                    else -> null
                }
            } ?: _formState.value.sourceId

            val newAmountText = payee.defaultAmount?.let { amt ->
                if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString()
            } ?: _formState.value.amountText

            _formState.value = _formState.value.copy(
                payeeId = payee.id,
                merchantName = payee.name,
                hasGstBill = payee.hasGstBill,
                gstNumber = payee.gstNumber ?: "",
                amountText = newAmountText,
                categoryId = payee.defaultCategoryId ?: _formState.value.categoryId,
                sourceId = matchedSourceId
            )
        } else {
            _formState.value = _formState.value.copy(
                payeeId = null
            )
        }
    }

    fun updateFormMerchant(merchant: String) {
        _formState.value = _formState.value.copy(
            merchantName = merchant,
            // If user manually edits merchant text and it diverges from selected payee name, reset payeeId
            payeeId = if (merchant != _formState.value.merchantName) null else _formState.value.payeeId
        )
    }

    fun updateFormHasGstBill(hasGst: Boolean) {
        _formState.value = _formState.value.copy(hasGstBill = hasGst)
    }

    fun updateFormGstNumber(gst: String) {
        _formState.value = _formState.value.copy(gstNumber = gst.uppercase())
    }

    fun updateFormNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun updateFormReceipt(uri: Uri?) {
        _formState.value = _formState.value.copy(receiptImageUri = uri)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = _formState.value
        val amount = state.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formState.value = state.copy(errorMessage = "Please enter a valid expense amount")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isSaving = true)
            val result = repository.saveTransaction(
                id = state.id,
                amount = amount,
                transactionDate = state.transactionDate,
                categoryId = state.categoryId,
                sourceId = state.sourceId,
                payeeId = state.payeeId,
                notes = state.notes,
                merchantName = state.merchantName.ifBlank { null },
                hasGstBill = state.hasGstBill,
                gstNumber = state.gstNumber.ifBlank { null },
                gstRate = state.gstRate,
                receiptImageUri = state.receiptImageUri
            )

            _formState.value = state.copy(isSaving = false)
            if (result.isSuccess) {
                _snackbarEvent.emit(if (state.id == null) "Expense logged successfully!" else "Expense updated!")
                onSuccess()
            } else {
                _formState.value = state.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    fun deleteTransaction(id: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(id)
            if (result.isSuccess) {
                _snackbarEvent.emit("Transaction deleted")
                onDeleted()
            }
        }
    }

    // Payees / Vendors
    fun savePayee(
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
        isDefault: Boolean = false,
        onSuccess: ((String) -> Unit)? = null
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val result = repository.savePayee(
                id = id,
                name = name,
                categoryTag = categoryTag,
                defaultCategoryId = defaultCategoryId,
                defaultPaymentSourceId = defaultPaymentSourceId,
                defaultPaymentMode = defaultPaymentMode,
                defaultAmount = defaultAmount,
                isRecurring = isRecurring,
                recurringFrequency = recurringFrequency,
                dueDayOfMonth = dueDayOfMonth,
                recurringType = recurringType,
                isFlexibleSchedule = isFlexibleSchedule,
                lastPaidDate = lastPaidDate,
                hasGstBill = hasGstBill,
                gstNumber = gstNumber,
                phoneOrContact = phoneOrContact,
                notes = notes,
                isDefault = isDefault
            )
            if (result.isSuccess) {
                val action = if (id != null) "updated" else "saved"
                _snackbarEvent.emit("Vendor '$name' $action")
                result.getOrNull()?.let { onSuccess?.invoke(it) }
            }
        }
    }

    fun deletePayee(id: String) {
        viewModelScope.launch {
            repository.deletePayee(id)
            _snackbarEvent.emit("Payee / Vendor removed")
        }
    }

    fun setDefaultPayee(id: String) {
        viewModelScope.launch {
            repository.setDefaultPayee(id)
            _snackbarEvent.emit("Default payee set")
        }
    }

    fun quickAddPayee(
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
        hasGst: Boolean = false,
        gstNumber: String? = null
    ) {
        savePayee(
            name = name,
            categoryTag = categoryTag,
            defaultCategoryId = defaultCategoryId,
            defaultPaymentSourceId = defaultPaymentSourceId,
            defaultPaymentMode = defaultPaymentMode,
            defaultAmount = defaultAmount,
            isRecurring = isRecurring,
            recurringFrequency = recurringFrequency,
            dueDayOfMonth = dueDayOfMonth,
            recurringType = recurringType,
            hasGstBill = hasGst,
            gstNumber = gstNumber,
            onSuccess = { newId ->
                val allSources = paymentSources.value
                val matchedSourceId = defaultPaymentSourceId?.takeIf { sId -> allSources.any { it.id == sId } }
                    ?: run {
                        val mode = (defaultPaymentMode ?: "").uppercase()
                        val tag = (categoryTag ?: "").uppercase()
                        when {
                            mode.contains("CC") || mode.contains("CREDIT") || tag.contains("FABRIC") || tag.contains("LINING") || tag.contains("ACCESSORIES") -> {
                                allSources.firstOrNull { it.type.equals("CC", ignoreCase = true) }?.id
                            }
                            mode.contains("UPI") || mode.contains("GPAY") || mode.contains("CASH") || tag.contains("RENT") -> {
                                allSources.firstOrNull {
                                    it.type.equals("UPI", ignoreCase = true) ||
                                    it.type.equals("CASH", ignoreCase = true) ||
                                    it.bankName.contains("UPI", ignoreCase = true) ||
                                    it.bankName.contains("Cash", ignoreCase = true)
                                }?.id
                            }
                            else -> null
                        }
                    } ?: _formState.value.sourceId

                val newAmountText = defaultAmount?.let { amt ->
                    if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString()
                } ?: _formState.value.amountText

                _formState.value = _formState.value.copy(
                    payeeId = newId,
                    merchantName = name,
                    hasGstBill = hasGst,
                    gstNumber = gstNumber ?: "",
                    amountText = newAmountText,
                    categoryId = defaultCategoryId ?: _formState.value.categoryId,
                    sourceId = matchedSourceId
                )
            }
        )
    }

    // Cash Projections & Recurring Debits Actions
    fun setLookaheadDays(days: Int) {
        val safeDays = days.coerceIn(1, 90)
        saveSetting("projection_lookahead_days", safeDays.toString())
    }

    fun setRecurringNotificationsEnabled(enabled: Boolean) {
        saveSetting("recurring_notifications_enabled", enabled.toString())
    }

    fun setRecurringAlertThreshold(days: Int) {
        val safeDays = days.coerceIn(1, 14)
        saveSetting("recurring_alert_threshold_days", safeDays.toString())
    }

    fun triggerProjectionNotification(isManualTest: Boolean = true) {
        val state = dashboardUiState.value
        val curSymbol = state.settingsMap["currency_symbol"] ?: "₹"
        val sent = notificationHelper.postProjectionAlert(
            projection = state.cashProjection,
            currencySymbol = curSymbol,
            isManualTest = isManualTest
        )
        viewModelScope.launch {
            if (sent) {
                _snackbarEvent.emit("🔔 Projection alert notification sent!")
            } else {
                _snackbarEvent.emit("Notification sent or permission requested")
            }
        }
    }

    fun prepareFormForRecurringDebit(debit: UpcomingDebitItem) {
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val cycleName = sdf.format(Date(debit.nextDueDate))
        val newAmtText = if (debit.amount % 1.0 == 0.0) debit.amount.toLong().toString() else debit.amount.toString()

        val allSources = paymentSources.value
        val matchedSourceId = debit.defaultPaymentSourceId?.takeIf { sId -> allSources.any { it.id == sId } }
            ?: when (debit.recurringType) {
                RecurringDebitType.RENT, RecurringDebitType.UTILITY -> {
                    allSources.firstOrNull { it.type == "UPI" || it.bankName.contains("UPI", ignoreCase = true) }?.id
                }
                RecurringDebitType.SALARY -> {
                    allSources.firstOrNull { it.type == "SB" || it.bankName.contains("State Bank", ignoreCase = true) }?.id
                }
                else -> allSources.firstOrNull { it.isDefault }?.id
            }

        _formState.value = _formState.value.copy(
            id = null,
            amountText = newAmtText,
            transactionDate = System.currentTimeMillis(),
            categoryId = debit.defaultCategoryId ?: categories.value.firstOrNull { it.name.contains(debit.recurringType.shortLabel, ignoreCase = true) }?.id,
            sourceId = matchedSourceId,
            payeeId = debit.payeeId,
            merchantName = debit.name,
            notes = "Recurring ${debit.recurringType.shortLabel} disbursement for $cycleName",
            errorMessage = null
        )
    }

    fun recordRecurringDebitDirectly(debit: UpcomingDebitItem) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val cycleName = sdf.format(Date(debit.nextDueDate))
            val allSources = paymentSources.value
            val targetSourceId = debit.defaultPaymentSourceId?.takeIf { sId -> allSources.any { it.id == sId } }
                ?: when (debit.recurringType) {
                    RecurringDebitType.RENT, RecurringDebitType.UTILITY -> {
                        allSources.firstOrNull { it.type == "UPI" || it.bankName.contains("UPI", ignoreCase = true) }?.id
                    }
                    RecurringDebitType.SALARY -> {
                        allSources.firstOrNull { it.type == "SB" }?.id
                    }
                    else -> allSources.firstOrNull { it.isDefault }?.id
                } ?: allSources.firstOrNull()?.id ?: ""

            val targetCategoryId = debit.defaultCategoryId
                ?: categories.value.firstOrNull { it.name.contains(debit.recurringType.shortLabel, ignoreCase = true) }?.id
                ?: categories.value.firstOrNull()?.id ?: ""

            val saveResult = repository.saveTransaction(
                id = null,
                amount = debit.amount,
                transactionDate = System.currentTimeMillis(),
                categoryId = targetCategoryId,
                sourceId = targetSourceId,
                payeeId = debit.payeeId,
                merchantName = debit.name,
                notes = "Settled recurring ${debit.recurringType.shortLabel} for $cycleName",
                hasGstBill = false,
                gstNumber = null,
                voucherType = "Payment",
                receiptImageUri = null
            )
            if (saveResult.isSuccess) {
                // Update payee's lastPaidDate
                val existingPayee = payees.value.firstOrNull { it.id == debit.payeeId }
                if (existingPayee != null) {
                    repository.savePayee(
                        id = existingPayee.id,
                        name = existingPayee.name,
                        categoryTag = existingPayee.categoryTag,
                        defaultCategoryId = existingPayee.defaultCategoryId,
                        defaultPaymentSourceId = existingPayee.defaultPaymentSourceId,
                        defaultPaymentMode = existingPayee.defaultPaymentMode,
                        defaultAmount = existingPayee.defaultAmount,
                        isRecurring = existingPayee.isRecurring,
                        recurringFrequency = existingPayee.recurringFrequency,
                        dueDayOfMonth = existingPayee.dueDayOfMonth,
                        recurringType = existingPayee.recurringType,
                        lastPaidDate = System.currentTimeMillis(),
                        hasGstBill = existingPayee.hasGstBill,
                        gstNumber = existingPayee.gstNumber,
                        phoneOrContact = existingPayee.phoneOrContact,
                        notes = existingPayee.notes,
                        isDefault = existingPayee.isDefault
                    )
                }
                val formattedAmt = if (debit.amount % 1.0 == 0.0) "₹${debit.amount.toLong()}" else "₹%.2f".format(debit.amount)
                _snackbarEvent.emit("Recorded $formattedAmt payment for ${debit.name}")
            }
        }
    }

    // Categories
    fun saveCategory(id: String? = null, name: String, icon: String = "category", color: String = "#0284C7") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveCategory(id = id, name = name, iconName = icon, colorHex = color)
            _snackbarEvent.emit("Expense Head '$name' saved")
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _snackbarEvent.emit("Category removed")
        }
    }

    // Payment Sources
    fun savePaymentSource(
        id: String? = null,
        type: String,
        bankName: String,
        cardName: String?,
        accountNumber: String?,
        ifscOrRouting: String?,
        last4Digits: String?,
        isDefault: Boolean
    ) {
        if (bankName.isBlank()) return
        viewModelScope.launch {
            repository.savePaymentSource(
                id = id,
                type = type,
                bankName = bankName,
                cardName = cardName,
                accountNumber = accountNumber,
                ifscOrRouting = ifscOrRouting,
                last4Digits = last4Digits,
                isDefault = isDefault
            )
            _snackbarEvent.emit("Payment Source saved")
        }
    }

    fun deletePaymentSource(id: String) {
        viewModelScope.launch {
            repository.deletePaymentSource(id)
            _snackbarEvent.emit("Payment source removed")
        }
    }

    fun setDefaultPaymentSource(id: String) {
        viewModelScope.launch {
            repository.setDefaultPaymentSource(id)
            _snackbarEvent.emit("Default payment source updated")
        }
    }

    // Settings
    fun saveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            _snackbarEvent.emit("Settings updated")
        }
    }

    fun saveSettings(settings: Map<String, String>) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _snackbarEvent.emit("All settings saved successfully")
        }
    }

    // Cloud Sync
    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncAllToCloud()
            _isSyncing.value = false
            when (result) {
                is SyncResult.Success -> _snackbarEvent.emit(result.message)
                is SyncResult.Error -> _snackbarEvent.emit(result.message)
            }
        }
    }

    // Exports
    suspend fun exportTallyCsv(transactions: List<TransactionWithDetails>): TallyExportResult {
        return repository.exportTallyCsv(transactions)
    }

    suspend fun exportTallyXml(transactions: List<TransactionWithDetails>): TallyExportResult {
        return repository.exportTallyXml(transactions)
    }

    suspend fun generateEmailReport(
        transactions: List<TransactionWithDetails>,
        dateLabel: String
    ): EmailReportPackage {
        return repository.generateEmailReport(transactions, dateLabel)
    }

    fun createTempCameraUri(): Uri = repository.createTempCameraUri()

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _snackbarEvent.emit("Sample vendors, categories, payment sources & expenses restored")
        }
    }

    companion object {
        private fun getStartOfDayTimestamp(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        private fun getEndOfDayTimestamp(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

        private fun getStartOfWeekTimestamp(): Long {
            val cal = Calendar.getInstance()
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        private fun getStartOfMonthTimestamp(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
