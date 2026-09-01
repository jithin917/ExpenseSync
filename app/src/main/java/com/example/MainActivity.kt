package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionWithDetails
import com.example.data.notification.RecurringDebitAlarmReceiver
import com.example.ui.components.ManageRecurringDebitsDialog
import com.example.ui.screens.AddEditExpenseScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmailReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TallyExportScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishNavBackground
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppDestination {
    DASHBOARD,
    ADD_EDIT_EXPENSE,
    TRANSACTION_DETAIL,
    TALLY_EXPORT,
    EMAIL_REPORT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule daily recurring debit projection check (9:00 AM)
        RecurringDebitAlarmReceiver.scheduleDailyProjectionCheck(this)

        setContent {
            MyApplicationTheme {
                ExpenseSyncApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExpenseSyncApp(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var selectedTransaction by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var showManageRecurringDialog by remember { mutableStateOf(false) }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.triggerProjectionNotification(isManualTest = true)
        }
    }

    // State flows
    val dashboardUiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val paymentSources by viewModel.paymentSources.collectAsStateWithLifecycle()
    val payees by viewModel.payees.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val currencySymbol = dashboardUiState.settingsMap["currency_symbol"] ?: "₹"

    // Snackbar event handler
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Hardware Back button handling
    BackHandler(enabled = currentScreen != AppDestination.DASHBOARD) {
        currentScreen = AppDestination.DASHBOARD
    }

    val showBottomNav = currentScreen in listOf(
        AppDestination.DASHBOARD,
        AppDestination.TALLY_EXPORT,
        AppDestination.EMAIL_REPORT,
        AppDestination.SETTINGS
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PolishBackground,
        bottomBar = {
            if (showBottomNav) {
                PolishBottomNavigationBar(
                    currentDestination = currentScreen,
                    onNavigate = { destination ->
                        currentScreen = destination
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppDestination.DASHBOARD -> {
                    DashboardScreen(
                        uiState = dashboardUiState,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onDatePresetSelect = { viewModel.setDatePreset(it) },
                        onCategoryFilterSelect = { viewModel.setCategoryFilter(it) },
                        onSourceFilterSelect = { viewModel.setSourceFilter(it) },
                        onTransactionClick = { txn ->
                            selectedTransaction = txn
                            currentScreen = AppDestination.TRANSACTION_DETAIL
                        },
                        onAddExpenseClick = {
                            viewModel.initAddForm()
                            currentScreen = AppDestination.ADD_EDIT_EXPENSE
                        },
                        onCameraSnapClick = { uri ->
                            viewModel.initAddForm(initialImageUri = uri)
                            currentScreen = AppDestination.ADD_EDIT_EXPENSE
                        },
                        onTallyExportClick = {
                            currentScreen = AppDestination.TALLY_EXPORT
                        },
                        onEmailReportClick = {
                            currentScreen = AppDestination.EMAIL_REPORT
                        },
                        onSettingsClick = {
                            currentScreen = AppDestination.SETTINGS
                        },
                        onSyncNowClick = {
                            viewModel.syncNow()
                        },
                        onLookaheadChange = { days ->
                            viewModel.setLookaheadDays(days)
                        },
                        onSendProjectionNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.triggerProjectionNotification(isManualTest = true)
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.triggerProjectionNotification(isManualTest = true)
                            }
                        },
                        onPayDebitClick = { debit ->
                            viewModel.prepareFormForRecurringDebit(debit)
                            currentScreen = AppDestination.ADD_EDIT_EXPENSE
                        },
                        onDirectRecordDebit = { debit ->
                            viewModel.recordRecurringDebitDirectly(debit)
                        },
                        onManageRecurringClick = {
                            showManageRecurringDialog = true
                        },
                        createTempCameraUri = {
                            viewModel.createTempCameraUri()
                        }
                    )
                }

                AppDestination.ADD_EDIT_EXPENSE -> {
                    AddEditExpenseScreen(
                        formState = formState,
                        categories = categories,
                        paymentSources = paymentSources,
                        payees = payees,
                        currencySymbol = currencySymbol,
                        onAmountChange = { viewModel.updateFormAmount(it) },
                        onDateChange = { viewModel.updateFormDate(it) },
                        onCategoryChange = { viewModel.updateFormCategory(it) },
                        onSourceChange = { viewModel.updateFormSource(it) },
                        onPayeeSelect = { viewModel.selectPayee(it) },
                        onMerchantChange = { viewModel.updateFormMerchant(it) },
                        onHasGstBillChange = { viewModel.updateFormHasGstBill(it) },
                        onGstNumberChange = { viewModel.updateFormGstNumber(it) },
                        onQuickAddPayee = { name, categoryTag, defaultCatId, defaultSourceId, defaultMode, hasGst, gstNum ->
                            viewModel.quickAddPayee(
                                name = name,
                                categoryTag = categoryTag,
                                defaultCategoryId = defaultCatId,
                                defaultPaymentSourceId = defaultSourceId,
                                defaultPaymentMode = defaultMode,
                                hasGst = hasGst,
                                gstNumber = gstNum
                            )
                        },
                        onNotesChange = { viewModel.updateFormNotes(it) },
                        onReceiptChange = { viewModel.updateFormReceipt(it) },
                        onSaveClick = {
                            viewModel.saveTransaction(
                                onSuccess = { currentScreen = AppDestination.DASHBOARD }
                            )
                        },
                        onCancelClick = {
                            currentScreen = AppDestination.DASHBOARD
                        },
                        createTempCameraUri = {
                            viewModel.createTempCameraUri()
                        }
                    )
                }

                AppDestination.TRANSACTION_DETAIL -> {
                    val txn = selectedTransaction
                    if (txn != null) {
                        TransactionDetailScreen(
                            transactionWithDetails = txn,
                            auditLogs = auditLogs,
                            currencySymbol = currencySymbol,
                            onBackClick = { currentScreen = AppDestination.DASHBOARD },
                            onEditClick = { item ->
                                viewModel.initEditForm(item)
                                currentScreen = AppDestination.ADD_EDIT_EXPENSE
                            },
                            onDeleteClick = { id ->
                                viewModel.deleteTransaction(
                                    id = id,
                                    onDeleted = { currentScreen = AppDestination.DASHBOARD }
                                )
                            }
                        )
                    } else {
                        currentScreen = AppDestination.DASHBOARD
                    }
                }

                AppDestination.TALLY_EXPORT -> {
                    TallyExportScreen(
                        transactions = dashboardUiState.transactions,
                        currencySymbol = currencySymbol,
                        onBackClick = { currentScreen = AppDestination.DASHBOARD },
                        onExportCsv = { txns -> viewModel.exportTallyCsv(txns) },
                        onExportXml = { txns -> viewModel.exportTallyXml(txns) }
                    )
                }

                AppDestination.EMAIL_REPORT -> {
                    EmailReportScreen(
                        transactions = dashboardUiState.transactions,
                        settingsMap = dashboardUiState.settingsMap,
                        currencySymbol = currencySymbol,
                        onBackClick = { currentScreen = AppDestination.DASHBOARD },
                        onGenerateReport = { txns, periodLabel ->
                            viewModel.generateEmailReport(txns, periodLabel)
                        }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        settingsMap = dashboardUiState.settingsMap,
                        categories = categories,
                        paymentSources = paymentSources,
                        payees = payees,
                        auditLogs = auditLogs,
                        isSyncing = isSyncing,
                        onBackClick = { currentScreen = AppDestination.DASHBOARD },
                        onSaveSettings = { settings -> viewModel.saveSettings(settings) },
                        onAddCategory = { name, icon, color -> viewModel.saveCategory(name = name, icon = icon, color = color) },
                        onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                        onAddPaymentSource = { type, bank, card, ac, ifsc, last4, isDefault ->
                            viewModel.savePaymentSource(
                                type = type,
                                bankName = bank,
                                cardName = card,
                                accountNumber = ac,
                                ifscOrRouting = ifsc,
                                last4Digits = last4,
                                isDefault = isDefault
                            )
                        },
                        onDeletePaymentSource = { id -> viewModel.deletePaymentSource(id) },
                        onSetDefaultSource = { id -> viewModel.setDefaultPaymentSource(id) },
                        onSavePayee = { id, name, categoryTag, defaultCategoryId, defaultPaymentSourceId, defaultPaymentMode, defaultAmount, isRecurring, recurringFrequency, dueDayOfMonth, recurringType, isFlexibleSchedule, hasGst, gstNum, phone, notes, isDefault ->
                            viewModel.savePayee(
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
                                hasGstBill = hasGst,
                                gstNumber = gstNum,
                                phoneOrContact = phone,
                                notes = notes,
                                isDefault = isDefault
                            )
                        },
                        onDeletePayee = { id -> viewModel.deletePayee(id) },
                        onSetDefaultPayee = { id -> viewModel.setDefaultPayee(id) },
                        onSyncNow = { viewModel.syncNow() },
                        onResetSampleData = { viewModel.resetSampleData() }
                    )
                }
            }

            // Manage Recurring Debits & Forecast Dialog
            if (showManageRecurringDialog) {
                ManageRecurringDebitsDialog(
                    projection = dashboardUiState.cashProjection,
                    payees = payees,
                    categories = categories,
                    paymentSources = paymentSources,
                    currencySymbol = currencySymbol,
                    onDismiss = { showManageRecurringDialog = false },
                    onSavePayee = { id, name, categoryTag, defaultCategoryId, defaultPaymentSourceId, defaultPaymentMode, defaultAmount, isRecurring, recurringFrequency, dueDayOfMonth, recurringType, isFlexibleSchedule, lastPaidDate, hasGst, gstNum, phone, notes, isDefault ->
                        viewModel.savePayee(
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
                            hasGstBill = hasGst,
                            gstNumber = gstNum,
                            phoneOrContact = phone,
                            notes = notes,
                            isDefault = isDefault
                        )
                    },
                    onDeletePayee = { id -> viewModel.deletePayee(id) },
                    onSaveLookaheadDays = { days -> viewModel.setLookaheadDays(days) },
                    onSaveNotificationsEnabled = { enabled -> viewModel.setRecurringNotificationsEnabled(enabled) },
                    onSaveAlertThreshold = { th -> viewModel.setRecurringAlertThreshold(th) },
                    onSendTestNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.triggerProjectionNotification(isManualTest = true)
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.triggerProjectionNotification(isManualTest = true)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PolishBottomNavigationBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    Surface(
        color = PolishNavBackground,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorder)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PolishNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentDestination == AppDestination.DASHBOARD,
                onClick = { onNavigate(AppDestination.DASHBOARD) },
                testTag = "nav_home"
            )

            PolishNavItem(
                icon = Icons.Default.ReceiptLong,
                label = "Tally",
                isSelected = currentDestination == AppDestination.TALLY_EXPORT,
                onClick = { onNavigate(AppDestination.TALLY_EXPORT) },
                testTag = "nav_tally"
            )

            PolishNavItem(
                icon = Icons.Default.ForwardToInbox,
                label = "Reports",
                isSelected = currentDestination == AppDestination.EMAIL_REPORT,
                onClick = { onNavigate(AppDestination.EMAIL_REPORT) },
                testTag = "nav_reports"
            )

            PolishNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentDestination == AppDestination.SETTINGS,
                onClick = { onNavigate(AppDestination.SETTINGS) },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
fun PolishNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 48.dp, height = 30.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandGreenLight)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = BrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(width = 48.dp, height = 30.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = PolishTextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandGreen else PolishTextSecondary.copy(alpha = 0.75f)
        )
    }
}


