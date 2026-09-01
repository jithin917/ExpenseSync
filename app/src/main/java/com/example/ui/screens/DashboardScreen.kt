package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.TransactionWithDetails
import com.example.data.model.UpcomingDebitItem
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.MonthlyExpenseBreakdownCard
import com.example.ui.components.ReceiptViewerDialog
import com.example.ui.components.RecurringProjectionCard
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatExpenseDateTime
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenSubtext
import com.example.ui.theme.BrandGreenTint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.DateFilterPreset
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onSearchChange: (String) -> Unit,
    onDatePresetSelect: (DateFilterPreset) -> Unit,
    onCategoryFilterSelect: (String?) -> Unit,
    onSourceFilterSelect: (String?) -> Unit,
    onTransactionClick: (TransactionWithDetails) -> Unit,
    onAddExpenseClick: () -> Unit,
    onCameraSnapClick: (Uri) -> Unit,
    onTallyExportClick: () -> Unit,
    onEmailReportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSyncNowClick: () -> Unit,
    onLookaheadChange: (Int) -> Unit = {},
    onSendProjectionNotification: () -> Unit = {},
    onPayDebitClick: (UpcomingDebitItem) -> Unit = {},
    onDirectRecordDebit: (UpcomingDebitItem) -> Unit = {},
    onManageRecurringClick: () -> Unit = {},
    createTempCameraUri: () -> Uri
) {
    val focusManager = LocalFocusManager.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var viewingReceiptPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onCameraSnapClick(tempCameraUri!!)
        }
    }

    val currencySymbol = uiState.settingsMap["currency_symbol"] ?: "₹"

    // Sync animation
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Scaffold(
        containerColor = PolishBackground,
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Camera Snap FAB
                FloatingActionButton(
                    onClick = {
                        val uri = createTempCameraUri()
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    containerColor = PolishSurface,
                    contentColor = BrandGreen,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                        .testTag("quick_snap_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Snap Receipt",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Log Expense Primary FAB (Squircle styling in #D1E8D1 / #386B3B)
                ExtendedFloatingActionButton(
                    onClick = onAddExpenseClick,
                    containerColor = BrandGreenLight,
                    contentColor = BrandGreenDark,
                    shape = RoundedCornerShape(18.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandGreenDark) },
                    text = { Text("Log Expense", fontWeight = FontWeight.Bold, color = BrandGreenDark) },
                    modifier = Modifier.testTag("add_expense_fab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section matching Professional Polish Design
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // "ES" Circular Brand Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandGreen)
                        ) {
                            Text(
                                text = "ES",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Column {
                            Text(
                                text = "ExpenseSync",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                color = PolishTextPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            val empName = uiState.settingsMap["employee_name"] ?: "Employee"
                            Text(
                                text = "$empName • ${uiState.settingsMap["company_name"] ?: "Acme"}",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }
                    }

                    // Action Icons (Sync / Notification bell & Profile)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sync / Status bell with indicator
                        Box(
                            modifier = Modifier
                                .clickable { onSyncNowClick() }
                                .testTag("top_bar_sync_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isSyncing) Icons.Default.Sync else Icons.Default.Notifications,
                                contentDescription = "Sync Notifications",
                                tint = PolishTextSecondary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (uiState.isSyncing) Modifier.rotate(spinAngle) else Modifier)
                            )
                            // Green status dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(if (uiState.pendingSyncCount > 0) PendingOrange else BrandGreen)
                                    .border(1.5.dp, PolishBackground, CircleShape)
                            )
                        }

                        // Profile Squircle Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PolishBorder)
                                .clickable { onSettingsClick() }
                                .testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Settings",
                                tint = PolishTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Hero Card: "Spent this month" in Soft Sage Green Container (#D1E8D1)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(BrandGreenLight)
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = "SPENT THIS MONTH",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandGreenSubtext,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(uiState.totalThisMonth, currencySymbol),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenDark
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Last synced",
                                        fontSize = 11.sp,
                                        color = BrandGreenSubtext.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (uiState.pendingSyncCount == 0) "All claims up to date" else "${uiState.pendingSyncCount} claims waiting",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandGreenDark
                                    )
                                }

                                // Tally Ready / Cloud Status Frosted Pill
                                Surface(
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.clickable { onSyncNowClick() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (uiState.pendingSyncCount == 0) BrandGreen else PendingOrange)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (uiState.pendingSyncCount == 0) "TALLY READY" else "SYNC PENDING",
                                            color = BrandGreenDark,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cash Projections & Recurring Debits (Salary, Rent, Utilities Forecast)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    RecurringProjectionCard(
                        projection = uiState.cashProjection,
                        currencySymbol = currencySymbol,
                        onLookaheadChange = onLookaheadChange,
                        onSendNotificationClick = onSendProjectionNotification,
                        onPayDebitClick = onPayDebitClick,
                        onDirectRecordClick = onDirectRecordDebit,
                        onManageRecurringClick = onManageRecurringClick
                    )
                }
            }

            // Monthly Expense Breakdown Data Visualization Component
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MonthlyExpenseBreakdownCard(
                        transactions = uiState.transactions,
                        categories = uiState.categories,
                        currencySymbol = currencySymbol,
                        selectedCategoryFilter = uiState.selectedCategoryFilter,
                        onCategoryClick = onCategoryFilterSelect
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search by merchant, ledger, notes...", color = PolishTextMuted, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PolishTextSecondary)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = PolishTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PolishSurface,
                            unfocusedContainerColor = PolishSurface,
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date range chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            DateFilterPreset.THIS_MONTH,
                            DateFilterPreset.THIS_WEEK,
                            DateFilterPreset.TODAY,
                            DateFilterPreset.ALL
                        ).forEach { preset ->
                            val isSelected = uiState.selectedDatePreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDatePresetSelect(preset) },
                                label = { Text(preset.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) BrandGreen else PolishBorder
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = PolishSurface,
                                    labelColor = PolishTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { onCategoryFilterSelect(null) },
                            label = { Text("All Categories") },
                            shape = RoundedCornerShape(12.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedCategoryFilter == null,
                                borderColor = if (uiState.selectedCategoryFilter == null) BrandGreenDark else PolishBorder
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGreenDark,
                                selectedLabelColor = Color.White,
                                containerColor = PolishSurface,
                                labelColor = PolishTextSecondary
                            )
                        )

                        for (cat in uiState.categories) {
                            val isSelected = uiState.selectedCategoryFilter == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategoryFilterSelect(if (isSelected) null else cat.id) },
                                label = { Text(cat.name) },
                                shape = RoundedCornerShape(12.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) BrandGreen else PolishBorder
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = PolishSurface,
                                    labelColor = PolishTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Recent Expenses Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Recent Expenses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextPrimary
                    )

                    Text(
                        text = "${uiState.filteredTransactions.size} items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen
                    )
                }
            }

            // Empty State
            if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PolishSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, PolishBorder, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = BrandGreenTint,
                                shape = CircleShape,
                                modifier = Modifier.size(60.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "No Expenses",
                                    tint = BrandGreen,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Expenses Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Log Expense' to record your vouchers and bills.",
                                fontSize = 13.sp,
                                color = PolishTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Transactions List Items matching the Polish style
                items(
                    items = uiState.filteredTransactions,
                    key = { it.transaction.id }
                ) { item ->
                    PolishTransactionListItemCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(item) },
                        onReceiptThumbnailClick = { path -> viewingReceiptPath = path },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    // Receipt viewer modal
    viewingReceiptPath?.let { path ->
        ReceiptViewerDialog(
            imagePath = path,
            onDismiss = { viewingReceiptPath = null }
        )
    }
}

@Composable
fun PolishTransactionListItemCard(
    item: TransactionWithDetails,
    currencySymbol: String,
    onClick: () -> Unit,
    onReceiptThumbnailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val txn = item.transaction
    val category = item.category
    val source = item.paymentSource
    val firstReceipt = item.receipts.firstOrNull()

    // Title: Payment Source / Account or Merchant
    val primaryTitle = if (source != null) {
        source.displayLabel
    } else {
        txn.merchantName?.takeIf { it.isNotBlank() } ?: (category?.name ?: "Expense")
    }

    // Subtitle: Merchant • Category
    val secondarySubtitle = if (txn.merchantName != null && txn.merchantName.isNotBlank() && source != null) {
        "${txn.merchantName} • ${category?.name ?: "Expense"}"
    } else {
        category?.name ?: "Expense"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("transaction_card_${txn.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Category Icon with Professional Polish container
            CategoryIconBadge(
                iconName = category?.iconName ?: "category",
                colorHex = category?.colorHex ?: "#386B3B"
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PolishTextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = secondarySubtitle,
                        fontSize = 12.sp,
                        color = PolishTextMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (item.payee?.categoryTag != null) {
                        Surface(
                            color = BrandGreenLight,
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = item.payee.categoryTag,
                                color = BrandGreenDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (txn.hasGstBill) {
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "GST",
                                color = Color(0xFF0369A1),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount in red/highlight & Date
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${formatCurrency(txn.amount, currencySymbol)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ExpenseRed
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatExpenseDateTime(txn.transactionDate),
                    fontSize = 10.sp,
                    color = PolishTextMuted
                )

                if (firstReceipt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val file = File(firstReceipt.localFilePath)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PolishBorder)
                            .clickable { onReceiptThumbnailClick(firstReceipt.localFilePath) }
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Receipt",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

