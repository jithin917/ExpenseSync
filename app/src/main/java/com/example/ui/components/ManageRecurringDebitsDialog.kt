package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.model.CashProjectionSummary
import com.example.data.model.RecurringDebitType
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenSubtext
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRecurringDebitsDialog(
    projection: CashProjectionSummary,
    payees: List<PayeeEntity>,
    categories: List<CategoryEntity>,
    paymentSources: List<PaymentSourceEntity>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSavePayee: (
        id: String?,
        name: String,
        categoryTag: String?,
        defaultCategoryId: String?,
        defaultPaymentSourceId: String?,
        defaultPaymentMode: String?,
        defaultAmount: Double?,
        isRecurring: Boolean,
        recurringFrequency: String?,
        dueDayOfMonth: Int?,
        recurringType: String?,
        isFlexibleSchedule: Boolean,
        lastPaidDate: Long?,
        hasGstBill: Boolean,
        gstNumber: String?,
        phoneOrContact: String?,
        notes: String?,
        isDefault: Boolean
    ) -> Unit,
    onDeletePayee: (String) -> Unit,
    onSaveLookaheadDays: (Int) -> Unit,
    onSaveNotificationsEnabled: (Boolean) -> Unit,
    onSaveAlertThreshold: (Int) -> Unit,
    onSendTestNotification: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Schedule & Debits, 1 = Config & Alerts
    var editingPayee by remember { mutableStateOf<PayeeEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("manage_recurring_dialog"),
            color = PolishSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Recurring Manager",
                                tint = BrandGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Recurring Debits & Projections",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Salaries, Rent, Utilities & Horizon Window",
                                fontSize = 12.sp,
                                color = PolishTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PolishTextSecondary
                        )
                    }
                }

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PolishSurface,
                    contentColor = BrandGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandGreen
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            isAddingNew = false
                            editingPayee = null
                        },
                        text = {
                            Text(
                                text = "Debits & Salaries (${payees.count { it.isRecurring || (it.defaultAmount ?: 0.0) > 0 }})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            isAddingNew = false
                            editingPayee = null
                        },
                        text = {
                            Text(
                                text = "Forecast & Notification Settings",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                // Content body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    if (isAddingNew || editingPayee != null) {
                        // Edit / Add Form
                        RecurringPayeeForm(
                            initialPayee = editingPayee,
                            categories = categories,
                            paymentSources = paymentSources,
                            currencySymbol = currencySymbol,
                            onCancel = {
                                isAddingNew = false
                                editingPayee = null
                            },
                            onSave = { entity ->
                                onSavePayee(
                                    entity.id.ifBlank { null },
                                    entity.name,
                                    entity.categoryTag,
                                    entity.defaultCategoryId,
                                    entity.defaultPaymentSourceId,
                                    entity.defaultPaymentMode,
                                    entity.defaultAmount,
                                    entity.isRecurring,
                                    entity.recurringFrequency,
                                    entity.dueDayOfMonth,
                                    entity.recurringType,
                                    entity.isFlexibleSchedule,
                                    entity.lastPaidDate,
                                    entity.hasGstBill,
                                    entity.gstNumber,
                                    entity.phoneOrContact,
                                    entity.notes,
                                    entity.isDefault
                                )
                                isAddingNew = false
                                editingPayee = null
                            }
                        )
                    } else if (selectedTab == 0) {
                        // Debits List
                        val recurringList = payees.filter { it.isRecurring || (it.defaultAmount ?: 0.0) > 0 }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Configured Recurring Obligations",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextSecondary
                                    )

                                    Button(
                                        onClick = {
                                            editingPayee = null
                                            isAddingNew = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("add_recurring_debit_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Recurring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (recurringList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No recurring debits configured yet. Tap 'Add Recurring' to add your rent, salaries, or recharges.",
                                            fontSize = 13.sp,
                                            color = PolishTextMuted,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(recurringList, key = { it.id }) { payee ->
                                    RecurringPayeeAdminCard(
                                        payee = payee,
                                        currencySymbol = currencySymbol,
                                        onEdit = { editingPayee = payee },
                                        onDelete = { onDeletePayee(payee.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Configuration & Notification Settings Tab
                        ProjectionSettingsTabContent(
                            projection = projection,
                            currencySymbol = currencySymbol,
                            onSaveLookaheadDays = onSaveLookaheadDays,
                            onSaveNotificationsEnabled = onSaveNotificationsEnabled,
                            onSaveAlertThreshold = onSaveAlertThreshold,
                            onSendTestNotification = onSendTestNotification
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringPayeeAdminCard(
    payee: PayeeEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val type = RecurringDebitType.fromString(payee.recurringType, payee.categoryTag, payee.name)
    val dueDay = payee.dueDayOfMonth ?: 1
    val amt = payee.defaultAmount ?: 0.0
    val amtStr = if (amt % 1.0 == 0.0) "$currencySymbol${amt.toLong()}" else "$currencySymbol%.2f".format(amt)

    Surface(
        color = PolishBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(android.graphics.Color.parseColor(type.colorHex)).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (type) {
                        RecurringDebitType.SALARY -> Icons.Default.Payments
                        RecurringDebitType.RENT -> Icons.Default.Hotel
                        RecurringDebitType.UTILITY -> Icons.Default.Bolt
                        RecurringDebitType.SUBSCRIPTION -> Icons.Default.Wifi
                        else -> Icons.Default.Schedule
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = type.title,
                        tint = Color(android.graphics.Color.parseColor(type.colorHex)),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = payee.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${type.title} • ${payee.recurringFrequency ?: "Monthly"}",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )

                        if (payee.isFlexibleSchedule) {
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "🔄 Flexible",
                                    color = Color(0xFF0369A1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "📅 Due ${dueDay}th",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    if (!payee.defaultPaymentMode.isNullOrBlank()) {
                        Text(
                            text = "Mode: ${payee.defaultPaymentMode}",
                            fontSize = 10.sp,
                            color = PolishTextMuted
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amtStr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PolishTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BrandGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectionSettingsTabContent(
    projection: CashProjectionSummary,
    currencySymbol: String,
    onSaveLookaheadDays: (Int) -> Unit,
    onSaveNotificationsEnabled: (Boolean) -> Unit,
    onSaveAlertThreshold: (Int) -> Unit,
    onSendTestNotification: () -> Unit
) {
    var customLookaheadInput by remember { mutableStateOf(projection.lookaheadDays.toString()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Lookahead Window Horizon Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PolishBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PolishBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROJECTION HORIZON (LOOKAHEAD WINDOW)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextSecondary,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "Calculate cash needed for the next ${projection.lookaheadDays} days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 7, 10, 14, 30, 45).forEach { days ->
                            val isSelected = projection.lookaheadDays == days
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    customLookaheadInput = days.toString()
                                    onSaveLookaheadDays(days)
                                },
                                label = { Text("$days Days", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = PolishSurface,
                                    labelColor = PolishTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customLookaheadInput,
                            onValueChange = { customLookaheadInput = it },
                            label = { Text("Custom Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(130.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PolishSurface,
                                unfocusedContainerColor = PolishSurface
                            )
                        )

                        Button(
                            onClick = {
                                val d = customLookaheadInput.toIntOrNull()
                                if (d != null && d > 0) {
                                    onSaveLookaheadDays(d)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }

        // Notification Alerts Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PolishBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PolishBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROJECTION NOTIFICATIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextSecondary,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Push alerts for upcoming salary, rent & debits",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = PolishTextPrimary
                            )
                        }

                        Switch(
                            checked = projection.notificationsEnabled,
                            onCheckedChange = { onSaveNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Alert Threshold (Days before due date):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 5, 7).forEach { thDays ->
                            val isSelected = projection.alertThresholdDays == thDays
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSaveAlertThreshold(thDays) },
                                label = { Text("$thDays Day${if (thDays > 1) "s" else ""}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = PolishSurface,
                                    labelColor = PolishTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onSendTestNotification,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Notification",
                            tint = BrandGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Test Projection Notification Now",
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringPayeeForm(
    initialPayee: PayeeEntity?,
    categories: List<CategoryEntity>,
    paymentSources: List<PaymentSourceEntity>,
    currencySymbol: String,
    onCancel: () -> Unit,
    onSave: (PayeeEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialPayee?.name ?: "") }
    var recurringType by remember {
        mutableStateOf(
            initialPayee?.recurringType ?: RecurringDebitType.fromString(null, initialPayee?.categoryTag, initialPayee?.name).name
        )
    }
    var amountText by remember {
        mutableStateOf(
            initialPayee?.defaultAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var dueDayOfMonth by remember {
        mutableStateOf(
            (initialPayee?.dueDayOfMonth ?: 1).toString()
        )
    }
    var isFlexibleSchedule by remember { mutableStateOf(initialPayee?.isFlexibleSchedule ?: false) }
    var recurringFrequency by remember { mutableStateOf(initialPayee?.recurringFrequency ?: "Monthly") }
    var selectedCategoryId by remember { mutableStateOf(initialPayee?.defaultCategoryId ?: categories.firstOrNull()?.id) }
    var selectedSourceId by remember { mutableStateOf(initialPayee?.defaultPaymentSourceId ?: paymentSources.firstOrNull()?.id) }
    var paymentModeText by remember { mutableStateOf(initialPayee?.defaultPaymentMode ?: "GPAY UPI / CASH") }
    var notes by remember { mutableStateOf(initialPayee?.notes ?: "") }
    var phone by remember { mutableStateOf(initialPayee?.phoneOrContact ?: "") }

    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var isSourceDropdownOpen by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Text(
                text = if (initialPayee != null) "Edit Recurring Debit / Payee" else "Add New Recurring Debit / Salary / Rent",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Vendor / Payee / Obligation Name *") },
                placeholder = { Text("e.g., Landlord Office Rent, Staff Salaries, KSEB") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("recurring_name_input")
            )
        }

        // Obligation Type Selector (Salary, Rent, Utilities, etc.)
        item {
            Column {
                Text(
                    text = "Recurring Obligation Type:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurringDebitType.entries.forEach { type ->
                        val isSelected = recurringType.equals(type.name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                recurringType = type.name
                                if (name.isBlank()) {
                                    when (type) {
                                        RecurringDebitType.RENT -> name = "Property Owner (Office Rent)"
                                        RecurringDebitType.SALARY -> name = "Staff Salaries / Payroll"
                                        RecurringDebitType.UTILITY -> name = "Electricity Board (Power Bill)"
                                        RecurringDebitType.SUBSCRIPTION -> name = "Broadband & Phone Recharge"
                                        else -> {}
                                    }
                                }
                            },
                            label = { Text(type.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGreen,
                                selectedLabelColor = Color.White,
                                containerColor = PolishSurface,
                                labelColor = PolishTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Recurring Amount ($currencySymbol) *") },
                    placeholder = { Text("15000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).testTag("recurring_amount_input")
                )

                OutlinedTextField(
                    value = dueDayOfMonth,
                    onValueChange = { dueDayOfMonth = it },
                    label = { Text("Due Day (1-31)") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.8f).testTag("recurring_dueday_input")
                )
            }
        }

        // Payment Strictness & Schedule Flexibility Selector
        item {
            Column {
                Text(
                    text = "Payment Schedule & Flexibility:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Option 1: Strict Due Date
                    Surface(
                        color = if (!isFlexibleSchedule) Color(0xFFF0FDF4) else PolishSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (!isFlexibleSchedule) 1.5.dp else 1.dp,
                            color = if (!isFlexibleSchedule) BrandGreen else PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFlexibleSchedule = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🗓️ Strict Due Date (On-Time Mandatory)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isFlexibleSchedule) BrandGreenDark else PolishTextPrimary
                                )
                                Text(
                                    text = "Rent, Staff Salaries, Power Bill, EMI. Overdue alerts & proactive notifications enabled.",
                                    fontSize = 11.sp,
                                    color = PolishTextSecondary
                                )
                            }
                            if (!isFlexibleSchedule) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Option 2: Flexible / As & When Required
                    Surface(
                        color = if (isFlexibleSchedule) Color(0xFFEFF6FF) else PolishSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isFlexibleSchedule) 1.5.dp else 1.dp,
                            color = if (isFlexibleSchedule) Color(0xFF2563EB) else PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFlexibleSchedule = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔄 Flexible / As & When Required",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFlexibleSchedule) Color(0xFF1D4ED8) else PolishTextPrimary
                                )
                                Text(
                                    text = "e.g., Secondary SIM not regularly used, on-demand recharge. No strict deadline or overdue warnings.",
                                    fontSize = 11.sp,
                                    color = PolishTextSecondary
                                )
                            }
                            if (isFlexibleSchedule) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = recurringFrequency,
                    onValueChange = { recurringFrequency = it },
                    label = { Text("Frequency") },
                    placeholder = { Text("Monthly") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = paymentModeText,
                    onValueChange = { paymentModeText = it },
                    label = { Text("Payment Mode") },
                    placeholder = { Text("GPAY UPI / CASH") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone / Reference No.") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Instructions") },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = PolishTextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val parsedAmt = amountText.toDoubleOrNull()
                            val parsedDueDay = dueDayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 1
                            val entity = (initialPayee ?: PayeeEntity(
                                id = "",
                                name = name
                            )).copy(
                                name = name,
                                defaultAmount = parsedAmt,
                                dueDayOfMonth = parsedDueDay,
                                recurringType = recurringType,
                                isRecurring = true,
                                recurringFrequency = recurringFrequency,
                                isFlexibleSchedule = isFlexibleSchedule,
                                defaultCategoryId = selectedCategoryId,
                                defaultPaymentSourceId = selectedSourceId,
                                defaultPaymentMode = paymentModeText,
                                phoneOrContact = phone,
                                notes = notes
                            )
                            onSave(entity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Obligation")
                }
            }
        }
    }
}
