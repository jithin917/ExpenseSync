package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenTint
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsMap: Map<String, String>,
    categories: List<CategoryEntity>,
    paymentSources: List<PaymentSourceEntity>,
    payees: List<PayeeEntity> = emptyList(),
    auditLogs: List<AuditLogEntity>,
    isSyncing: Boolean,
    onBackClick: () -> Unit,
    onSaveSettings: (Map<String, String>) -> Unit,
    onAddCategory: (String, String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onAddPaymentSource: (type: String, bank: String, card: String?, ac: String?, ifsc: String?, last4: String?, isDefault: Boolean) -> Unit,
    onDeletePaymentSource: (String) -> Unit,
    onSetDefaultSource: (String) -> Unit,
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
        hasGst: Boolean,
        gstNum: String?,
        phone: String?,
        notes: String?,
        isDefault: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onDeletePayee: (String) -> Unit = {},
    onSetDefaultPayee: (String) -> Unit = {},
    onSyncNow: () -> Unit,
    onResetSampleData: () -> Unit
) {
    var employeeName by remember(settingsMap) { mutableStateOf(settingsMap["employee_name"] ?: "Jithin Kumar") }
    var employeeId by remember(settingsMap) { mutableStateOf(settingsMap["employee_id"] ?: "EMP-204") }
    var companyName by remember(settingsMap) { mutableStateOf(settingsMap["company_name"] ?: "Acme Corporation") }
    var accountantEmail by remember(settingsMap) { mutableStateOf(settingsMap["accountant_email"] ?: "accounts@acmecorp.com") }
    var emailFrequency by remember(settingsMap) { mutableStateOf(settingsMap["email_frequency"] ?: "Daily batch") }
    var currencySymbol by remember(settingsMap) { mutableStateOf(settingsMap["currency_symbol"] ?: "₹") }

    // Category modal
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf("category") }
    var newCatColor by remember { mutableStateOf("#386B3B") }

    // Payment Source modal
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var newSourceType by remember { mutableStateOf("CC") }
    var newSourceBank by remember { mutableStateOf("") }
    var newSourceCardName by remember { mutableStateOf("") }
    var newSourceAccountNo by remember { mutableStateOf("") }
    var newSourceIfsc by remember { mutableStateOf("") }
    var newSourceLast4 by remember { mutableStateOf("") }
    var newSourceIsDefault by remember { mutableStateOf(false) }

    // Payee / Vendor modal
    var showAddPayeeDialog by remember { mutableStateOf(false) }
    var editingPayeeId by remember { mutableStateOf<String?>(null) }
    var newPayeeName by remember { mutableStateOf("") }
    var newPayeeCategoryTag by remember { mutableStateOf("") }
    var newPayeeCategoryId by remember { mutableStateOf<String?>(null) }
    var newPayeePaymentMode by remember { mutableStateOf("Credit Card (CC)") }
    var newPayeePaymentSourceId by remember { mutableStateOf<String?>(null) }
    var newPayeeDefaultAmount by remember { mutableStateOf("") }
    var newPayeeIsRecurring by remember { mutableStateOf(false) }
    var newPayeeRecurringFreq by remember { mutableStateOf("Monthly") }
    var newPayeeDueDayOfMonth by remember { mutableStateOf("1") }
    var newPayeeRecurringType by remember { mutableStateOf("OTHER") }
    var newPayeeIsFlexibleSchedule by remember { mutableStateOf(false) }
    var newPayeeHasGst by remember { mutableStateOf(false) }
    var newPayeeGstNum by remember { mutableStateOf("") }
    var newPayeePhone by remember { mutableStateOf("") }
    var newPayeeIsDefault by remember { mutableStateOf(false) }
    var newPayeeNotes by remember { mutableStateOf("") }

    Scaffold(
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Accounting Config", fontWeight = FontWeight.Bold, color = PolishTextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onSaveSettings(
                                mapOf(
                                    "employee_name" to employeeName,
                                    "employee_id" to employeeId,
                                    "company_name" to companyName,
                                    "accountant_email" to accountantEmail,
                                    "email_frequency" to emailFrequency,
                                    "currency_symbol" to currencySymbol
                                )
                            )
                        },
                        modifier = Modifier.testTag("save_all_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = BrandGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishBackground,
                    titleContentColor = PolishTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Employee Profile Section
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EMPLOYEE PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = employeeName,
                        onValueChange = { employeeName = it },
                        label = { Text("Employee Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = BrandGreen) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_employee_name")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID / Code") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "ID", tint = BrandGreen) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_employee_id")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company / Organization") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Company", tint = BrandGreen) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_company_name")
                    )
                }
            }

            // Payees & Vendors Section (Rent, Fabrics, Lining, Accessories, etc.)
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FREQUENT VENDORS & PAYEES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Configured vendors with auto-filled GSTIN & tags",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                editingPayeeId = null
                                newPayeeName = ""
                                newPayeeCategoryTag = ""
                                newPayeeCategoryId = categories.firstOrNull()?.id
                                newPayeePaymentMode = "Credit Card (CC)"
                                newPayeePaymentSourceId = paymentSources.firstOrNull { it.type == "CC" }?.id
                                newPayeeDefaultAmount = ""
                                newPayeeIsRecurring = false
                                newPayeeRecurringFreq = "Monthly"
                                newPayeeHasGst = false
                                newPayeeGstNum = ""
                                newPayeePhone = ""
                                newPayeeIsDefault = false
                                newPayeeNotes = ""
                                showAddPayeeDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_vendor_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Vendor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (payees.isEmpty()) {
                        Surface(
                            color = PolishHover,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No frequent payees added yet. Tap 'Add Vendor' to configure recurring payees like Rent, Olive Fashion, Radhakrishna Textiles.",
                                fontSize = 12.sp,
                                color = PolishTextMuted,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (payee in payees) {
                                val paymentModeBadge = when {
                                    payee.defaultPaymentMode?.contains("CC", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("Credit", ignoreCase = true) == true ||
                                    payee.categoryTag.equals("Fabric", ignoreCase = true) ||
                                    payee.categoryTag.equals("Lining", ignoreCase = true) ||
                                    payee.categoryTag.equals("Accessories", ignoreCase = true) -> "💳 Credit Card (CC)"
                                    payee.defaultPaymentMode?.contains("UPI", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("GPAY", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("Cash", ignoreCase = true) == true ||
                                    payee.categoryTag.equals("Rent", ignoreCase = true) ||
                                    payee.name.contains("Rent", ignoreCase = true) -> "📱 GPAY UPI / CASH"
                                    payee.defaultPaymentMode?.contains("SB", ignoreCase = true) == true -> "🏦 Savings Bank (SB)"
                                    else -> payee.defaultPaymentMode
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PolishHover, RoundedCornerShape(10.dp))
                                        .clickable {
                                            // Click to edit payee
                                            editingPayeeId = payee.id
                                            newPayeeName = payee.name
                                            newPayeeCategoryTag = payee.categoryTag ?: ""
                                            newPayeeCategoryId = payee.defaultCategoryId
                                            newPayeePaymentMode = payee.defaultPaymentMode ?: "Credit Card (CC)"
                                            newPayeePaymentSourceId = payee.defaultPaymentSourceId
                                            newPayeeDefaultAmount = payee.defaultAmount?.let {
                                                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                                            } ?: ""
                                            newPayeeIsRecurring = payee.isRecurring || (payee.defaultAmount != null && payee.defaultAmount > 0)
                                            newPayeeRecurringFreq = payee.recurringFrequency ?: "Monthly"
                                            newPayeeHasGst = payee.hasGstBill
                                            newPayeeGstNum = payee.gstNumber ?: ""
                                            newPayeePhone = payee.phoneOrContact ?: ""
                                            newPayeeIsDefault = payee.isDefault
                                            newPayeeNotes = payee.notes ?: ""
                                            showAddPayeeDialog = true
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("payee_row_${payee.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Payee",
                                        tint = BrandGreen,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = payee.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = PolishTextPrimary
                                            )

                                            if (!payee.categoryTag.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = BrandGreenLight,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = payee.categoryTag,
                                                        color = BrandGreenDark,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            if (payee.defaultAmount != null && payee.defaultAmount > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFE0F2FE),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    val amtDisplay = if (payee.defaultAmount % 1.0 == 0.0) "₹${payee.defaultAmount.toLong()}" else "₹%.2f".format(payee.defaultAmount)
                                                    val freqSuffix = if (payee.isRecurring && !payee.recurringFrequency.isNullOrBlank()) "/${payee.recurringFrequency.take(2).lowercase()}" else ""
                                                    Text(
                                                        text = "🔁 $amtDisplay$freqSuffix",
                                                        color = Color(0xFF0369A1),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            if (payee.isDefault) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Default",
                                                        color = Color(0xFFB45309),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            if (payee.isRecurring || (payee.defaultAmount != null && payee.defaultAmount > 0)) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                if (payee.isFlexibleSchedule) {
                                                    Surface(
                                                        color = Color(0xFFE0F2FE),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "🔄 Flexible / As Needed",
                                                            color = Color(0xFF0369A1),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                } else {
                                                    Surface(
                                                        color = Color(0xFFDCFCE7),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "📅 Due: ${payee.dueDayOfMonth ?: 1}th",
                                                            color = Color(0xFF15803D),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (paymentModeBadge != null) {
                                                Surface(
                                                    color = Color(0xFFF3E8FF),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = paymentModeBadge,
                                                        color = Color(0xFF7C3AED),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            if (payee.hasGstBill) {
                                                Text(
                                                    text = "GSTIN: ${payee.gstNumber ?: "Registered"}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF0284C7),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            } else {
                                                Text(
                                                    text = "Non-GST / Personal",
                                                    fontSize = 11.sp,
                                                    color = PolishTextMuted
                                                )
                                            }
                                        }
                                    }

                                    if (!payee.isDefault) {
                                        TextButton(onClick = { onSetDefaultPayee(payee.id) }) {
                                            Text("Default", fontSize = 11.sp, color = BrandGreen)
                                        }
                                    }

                                    // Edit Vendor Button
                                    IconButton(
                                        onClick = {
                                            editingPayeeId = payee.id
                                            newPayeeName = payee.name
                                            newPayeeCategoryTag = payee.categoryTag ?: ""
                                            newPayeeCategoryId = payee.defaultCategoryId
                                            newPayeePaymentMode = payee.defaultPaymentMode ?: "Credit Card (CC)"
                                            newPayeePaymentSourceId = payee.defaultPaymentSourceId
                                            newPayeeDefaultAmount = payee.defaultAmount?.let {
                                                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                                            } ?: ""
                                            newPayeeIsRecurring = payee.isRecurring || (payee.defaultAmount != null && payee.defaultAmount > 0)
                                            newPayeeRecurringFreq = payee.recurringFrequency ?: "Monthly"
                                            newPayeeDueDayOfMonth = (payee.dueDayOfMonth ?: 1).toString()
                                            newPayeeRecurringType = payee.recurringType ?: "OTHER"
                                            newPayeeIsFlexibleSchedule = payee.isFlexibleSchedule
                                            newPayeeHasGst = payee.hasGstBill
                                            newPayeeGstNum = payee.gstNumber ?: ""
                                            newPayeePhone = payee.phoneOrContact ?: ""
                                            newPayeeIsDefault = payee.isDefault
                                            newPayeeNotes = payee.notes ?: ""
                                            showAddPayeeDialog = true
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("edit_payee_${payee.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Vendor",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeletePayee(payee.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = PolishTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Accounting & Dispatch Section
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNTING & EMAIL DISPATCH CONFIG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = accountantEmail,
                        onValueChange = { accountantEmail = it },
                        label = { Text("Accountant / Accounts Email ID") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = BrandGreen) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_accountant_email")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Automated Email Frequency",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Daily batch", "Weekly summary", "Monthly closing").forEach { freq ->
                            val isSelected = emailFrequency == freq
                            Surface(
                                color = if (isSelected) BrandGreenLight else PolishHover,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { emailFrequency = freq }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = freq,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandGreenDark else PolishTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency Symbol") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Categories (Tally Expense Heads)
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EXPENSE HEADS / TALLY LEDGERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${categories.size} accounting heads mapped",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                newCatName = ""
                                showAddCategoryDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_category_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Head", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (cat in categories) {
                            Surface(
                                color = PolishHover,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    CategoryIconBadge(iconName = cat.iconName, colorHex = cat.colorHex, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PolishTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteCategory(cat.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = PolishTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Payment Sources (Bank Accounts & Credit Cards)
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PAYMENT LEDGERS (SB & CC)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${paymentSources.size} sources configured",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                newSourceBank = ""
                                newSourceCardName = ""
                                newSourceAccountNo = ""
                                newSourceIfsc = ""
                                newSourceLast4 = ""
                                newSourceIsDefault = false
                                showAddSourceDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_payment_source_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Source", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (src in paymentSources) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PolishHover, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("source_row_${src.id}")
                            ) {
                                Icon(
                                    imageVector = if (src.type == "CC") Icons.Default.CreditCard else Icons.Default.Payment,
                                    contentDescription = src.type,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = src.displayLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PolishTextPrimary
                                        )
                                        if (src.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = BrandGreenLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Default",
                                                    color = BrandGreenDark,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (src.type == "CC") "Credit Card (Last 4: ${src.last4Digits ?: "N/A"})" else "Savings Bank",
                                        fontSize = 11.sp,
                                        color = PolishTextMuted
                                    )
                                }

                                if (!src.isDefault) {
                                    TextButton(onClick = { onSetDefaultSource(src.id) }) {
                                        Text("Set Default", fontSize = 11.sp, color = BrandGreen)
                                    }
                                }

                                IconButton(
                                    onClick = { onDeletePaymentSource(src.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = PolishTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cloud Database & Tools
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CLOUD PERSISTENCE & DATABASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_to_cloud_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync All to Cloud Database (Firestore)")
                        }
                    }

                    OutlinedButton(
                        onClick = onResetSampleData,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = PolishTextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Default Categories & Demo Expenses", color = PolishTextSecondary)
                    }
                }
            }

            // Save Settings Primary Button
            Button(
                onClick = {
                    onSaveSettings(
                        mapOf(
                            "employee_name" to employeeName,
                            "employee_id" to employeeId,
                            "company_name" to companyName,
                            "accountant_email" to accountantEmail,
                            "email_frequency" to emailFrequency,
                            "currency_symbol" to currencySymbol
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_all_settings_footer_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add / Edit Payee Dialog
    if (showAddPayeeDialog) {
        val isEditing = editingPayeeId != null
        AlertDialog(
            onDismissRequest = { showAddPayeeDialog = false },
            title = {
                Text(
                    text = if (isEditing) "Edit Vendor & Settings" else "Add Frequent Vendor / Payee",
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newPayeeName,
                        onValueChange = { newPayeeName = it },
                        label = { Text("Vendor / Payee Name *") },
                        placeholder = { Text("e.g. Radhakrishna Textiles, Property Owner") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_name_field")
                    )

                    OutlinedTextField(
                        value = newPayeeCategoryTag,
                        onValueChange = { newPayeeCategoryTag = it },
                        label = { Text("Item / Material Tag (e.g. Lining, Fabric, Rent)") },
                        placeholder = { Text("e.g. Lining, Fabric, Accessories, Rent") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_tag_field")
                    )

                    // Recurring Amount Section
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Recurring / Fixed Amount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BrandGreenDark
                                    )
                                    Text(
                                        text = "Auto-fills amount for fixed expenses (e.g. Rent ₹15,000)",
                                        fontSize = 11.sp,
                                        color = PolishTextMuted
                                    )
                                }
                                Switch(
                                    checked = newPayeeIsRecurring || newPayeeDefaultAmount.isNotBlank(),
                                    onCheckedChange = { isChecked ->
                                        newPayeeIsRecurring = isChecked
                                        if (!isChecked) {
                                            newPayeeDefaultAmount = ""
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen),
                                    modifier = Modifier.testTag("vendor_recurring_switch")
                                )
                            }

                            if (newPayeeIsRecurring || newPayeeDefaultAmount.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = newPayeeDefaultAmount,
                                    onValueChange = { newPayeeDefaultAmount = it },
                                    label = { Text("Recurring Amount ($currencySymbol) *") },
                                    placeholder = { Text("e.g. 15000") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandGreen,
                                        focusedLabelColor = BrandGreen
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("vendor_recurring_amount_field")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Obligation Category Type Selector
                                Text(
                                    text = "OBLIGATION CATEGORY / TYPE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val obligationTypes = listOf(
                                    "RENT" to "🏢 Rent",
                                    "SALARY" to "👥 Salary",
                                    "UTILITY" to "⚡ Electricity / Power",
                                    "SUBSCRIPTION" to "📱 SIM / Broadband",
                                    "EMI" to "🏦 Loan EMI",
                                    "VENDOR" to "📦 Vendor Retainer",
                                    "OTHER" to "🗓️ Other Recurring"
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((typeKey, typeLabel) in obligationTypes) {
                                        val isTypeSelected = newPayeeRecurringType.equals(typeKey, ignoreCase = true)
                                        Surface(
                                            color = if (isTypeSelected) BrandGreen else Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isTypeSelected) BrandGreen else PolishBorder
                                            ),
                                            modifier = Modifier.clickable { newPayeeRecurringType = typeKey }
                                        ) {
                                            Text(
                                                text = typeLabel,
                                                fontSize = 11.sp,
                                                fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isTypeSelected) Color.White else PolishTextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Due Date Marking (Day of Month)
                                Text(
                                    text = "DUE DATE (DAY OF MONTH)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newPayeeDueDayOfMonth,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || (input.toIntOrNull() != null && input.toInt() in 1..31)) {
                                                newPayeeDueDayOfMonth = input
                                            }
                                        },
                                        label = { Text("Due Day (1-31)") },
                                        placeholder = { Text("1") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGreen,
                                            focusedLabelColor = BrandGreen
                                        ),
                                        modifier = Modifier.width(130.dp).testTag("vendor_due_day_field")
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Quick Select:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PolishTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(1, 5, 7, 10, 15, 20, 25, 30).forEach { day ->
                                                val isDaySelected = newPayeeDueDayOfMonth == day.toString()
                                                Surface(
                                                    color = if (isDaySelected) BrandGreen else Color.White,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isDaySelected) BrandGreen else PolishBorder
                                                    ),
                                                    modifier = Modifier.clickable { newPayeeDueDayOfMonth = day.toString() }
                                                ) {
                                                    Text(
                                                        text = "${day}th",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isDaySelected) Color.White else PolishTextPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📅 Due on ${newPayeeDueDayOfMonth.ifBlank { "1" }}th of every month. Advance alerts will notify you prior to this due date.",
                                    fontSize = 11.sp,
                                    color = BrandGreenDark,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Payment Strictness & Schedule Flexibility
                                Text(
                                    text = "PAYMENT SCHEDULE & FLEXIBILITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Option 1: Strict Due Date
                                    Surface(
                                        color = if (!newPayeeIsFlexibleSchedule) Color(0xFFF0FDF4) else Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (!newPayeeIsFlexibleSchedule) 1.5.dp else 1.dp,
                                            color = if (!newPayeeIsFlexibleSchedule) BrandGreen else PolishBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { newPayeeIsFlexibleSchedule = false }
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
                                                    color = if (!newPayeeIsFlexibleSchedule) BrandGreenDark else PolishTextPrimary
                                                )
                                                Text(
                                                    text = "e.g. Rent, Staff Salaries, Power Bill, EMI. Must be paid on time with proactive advance reminders & overdue warnings.",
                                                    fontSize = 10.5.sp,
                                                    color = PolishTextSecondary
                                                )
                                            }
                                            if (!newPayeeIsFlexibleSchedule) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = BrandGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Option 2: Flexible / As and when required
                                    Surface(
                                        color = if (newPayeeIsFlexibleSchedule) Color(0xFFEFF6FF) else Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (newPayeeIsFlexibleSchedule) 1.5.dp else 1.dp,
                                            color = if (newPayeeIsFlexibleSchedule) Color(0xFF2563EB) else PolishBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { newPayeeIsFlexibleSchedule = true }
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
                                                    color = if (newPayeeIsFlexibleSchedule) Color(0xFF1D4ED8) else PolishTextPrimary
                                                )
                                                Text(
                                                    text = "e.g. Secondary SIM not regularly used, on-demand recharge, variable supplies. No strict deadline or overdue nagging.",
                                                    fontSize = 10.5.sp,
                                                    color = PolishTextSecondary
                                                )
                                            }
                                            if (newPayeeIsFlexibleSchedule) {
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

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "FREQUENCY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val frequencies = listOf("Monthly", "Quarterly", "Annual", "Fixed / Per Bill")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (freq in frequencies) {
                                        val isFreqSelected = newPayeeRecurringFreq == freq
                                        Surface(
                                            color = if (isFreqSelected) BrandGreen else Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isFreqSelected) BrandGreen else PolishBorder
                                            ),
                                            modifier = Modifier.clickable { newPayeeRecurringFreq = freq }
                                        ) {
                                            Text(
                                                text = freq,
                                                fontSize = 11.sp,
                                                fontWeight = if (isFreqSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isFreqSelected) Color.White else PolishTextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Default Mode of Payment
                    Column {
                        Text(
                            text = "DEFAULT MODE OF PAYMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val paymentModes = listOf(
                            "Credit Card (CC)" to "💳 Credit Card (Fabric/Material)",
                            "GPAY UPI / CASH" to "📱 GPAY UPI / Cash (Rent/Direct)",
                            "Savings Bank (SB)" to "🏦 Savings Bank (SB / NEFT)"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for ((modeKey, modeLabel) in paymentModes) {
                                val isSelected = newPayeePaymentMode == modeKey
                                Surface(
                                    color = if (isSelected) BrandGreenTint else PolishHover,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) BrandGreen else PolishBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            newPayeePaymentMode = modeKey
                                            newPayeePaymentSourceId = when (modeKey) {
                                                "Credit Card (CC)" -> paymentSources.firstOrNull { it.type == "CC" }?.id
                                                "GPAY UPI / CASH" -> paymentSources.firstOrNull { it.type == "UPI" || it.type == "CASH" }?.id
                                                else -> paymentSources.firstOrNull { it.type == "SB" }?.id
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = modeLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) BrandGreen else PolishTextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = BrandGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Default Category Head
                    if (categories.isNotEmpty()) {
                        Column {
                            Text(
                                text = "DEFAULT EXPENSE HEAD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (cat in categories) {
                                    val isCatSelected = newPayeeCategoryId == cat.id
                                    Surface(
                                        color = if (isCatSelected) BrandGreen else PolishHover,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { newPayeeCategoryId = cat.id }
                                    ) {
                                        Text(
                                            text = cat.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCatSelected) Color.White else PolishTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GST Bill Provided?", fontSize = 13.sp, color = PolishTextPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = newPayeeHasGst,
                            onCheckedChange = { newPayeeHasGst = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen)
                        )
                    }

                    if (newPayeeHasGst) {
                        OutlinedTextField(
                            value = newPayeeGstNum,
                            onValueChange = { newPayeeGstNum = it.uppercase() },
                            label = { Text("Vendor GSTIN Number") },
                            placeholder = { Text("e.g. 32AAAAA0000A1Z5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vendor_gstin_field")
                        )
                    }

                    OutlinedTextField(
                        value = newPayeePhone,
                        onValueChange = { newPayeePhone = it },
                        label = { Text("Phone / Contact (Optional)") },
                        placeholder = { Text("e.g. +91 98765 43210") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as default payee", fontSize = 13.sp, color = PolishTextPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = newPayeeIsDefault,
                            onCheckedChange = { newPayeeIsDefault = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen)
                        )
                    }

                    OutlinedTextField(
                        value = newPayeeNotes,
                        onValueChange = { newPayeeNotes = it },
                        label = { Text("Notes / Description (Optional)") },
                        placeholder = { Text("e.g. Regular vendor for raw materials & buttons") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPayeeName.isNotBlank()) {
                            val parsedAmount = newPayeeDefaultAmount.trim().toDoubleOrNull()
                            val isRec = newPayeeIsRecurring || parsedAmount != null
                            val dueDay = if (isRec) newPayeeDueDayOfMonth.trim().toIntOrNull()?.coerceIn(1, 31) ?: 1 else null
                            val recType = if (isRec) newPayeeRecurringType.trim().ifBlank { "OTHER" } else null
                            onSavePayee(
                                editingPayeeId,
                                newPayeeName.trim(),
                                newPayeeCategoryTag.trim().ifBlank { null },
                                newPayeeCategoryId,
                                newPayeePaymentSourceId,
                                newPayeePaymentMode,
                                parsedAmount,
                                isRec,
                                if (parsedAmount != null) newPayeeRecurringFreq else null,
                                dueDay,
                                recType,
                                if (isRec) newPayeeIsFlexibleSchedule else false,
                                newPayeeHasGst,
                                newPayeeGstNum.trim().ifBlank { null },
                                newPayeePhone.trim().ifBlank { null },
                                newPayeeNotes.trim().ifBlank { null },
                                newPayeeIsDefault
                            )
                            showAddPayeeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.testTag("save_vendor_confirm_button")
                ) {
                    Text(if (isEditing) "Save Changes" else "Add Vendor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPayeeDialog = false }) {
                    Text("Cancel", color = PolishTextSecondary)
                }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Expense Head (Category)", color = PolishTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category / Ledger Name") },
                        placeholder = { Text("e.g. Fuel & Conveyance") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Icon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PolishTextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("flight", "restaurant", "inventory", "hotel", "celebration", "terminal").forEach { iconName ->
                            val isSelected = newCatIcon == iconName
                            Surface(
                                color = if (isSelected) BrandGreen else PolishHover,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { newCatIcon = iconName }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CategoryIconBadge(iconName = iconName, colorHex = if (isSelected) "#FFFFFF" else newCatColor, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddCategory(newCatName, newCatIcon, newCatColor)
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = PolishTextSecondary)
                }
            }
        )
    }

    // Add Payment Source Dialog
    if (showAddSourceDialog) {
        AlertDialog(
            onDismissRequest = { showAddSourceDialog = false },
            title = { Text("Add Payment Source", color = PolishTextPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PolishTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { newSourceType = "CC" }
                        ) {
                            RadioButton(
                                selected = newSourceType == "CC",
                                onClick = { newSourceType = "CC" },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandGreen)
                            )
                            Text("Credit Card (CC)", color = PolishTextPrimary)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { newSourceType = "SB" }
                        ) {
                            RadioButton(
                                selected = newSourceType == "SB",
                                onClick = { newSourceType = "SB" },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandGreen)
                            )
                            Text("Bank (SB)", color = PolishTextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = newSourceBank,
                        onValueChange = { newSourceBank = it },
                        label = { Text("Bank / Institution Name") },
                        placeholder = { Text("e.g. HDFC Bank, Chase") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (newSourceType == "CC") {
                        OutlinedTextField(
                            value = newSourceCardName,
                            onValueChange = { newSourceCardName = it },
                            label = { Text("Card Name / Variant (Optional)") },
                            placeholder = { Text("e.g. Corporate Platinum") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newSourceLast4,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newSourceLast4 = it },
                            label = { Text("Last 4 Digits (Secure)") },
                            placeholder = { Text("e.g. 4892") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = newSourceAccountNo,
                            onValueChange = { newSourceAccountNo = it },
                            label = { Text("Account Identifier") },
                            placeholder = { Text("e.g. **** 5678 or Cash Desk") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newSourceIfsc,
                            onValueChange = { newSourceIfsc = it },
                            label = { Text("IFSC / Routing Code (Optional)") },
                            placeholder = { Text("e.g. HDFC0000123") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as default payment source", fontSize = 13.sp, color = PolishTextPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = newSourceIsDefault,
                            onCheckedChange = { newSourceIsDefault = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSourceBank.isNotBlank()) {
                            onAddPaymentSource(
                                newSourceType,
                                newSourceBank,
                                newSourceCardName.ifBlank { null },
                                newSourceAccountNo.ifBlank { null },
                                newSourceIfsc.ifBlank { null },
                                newSourceLast4.ifBlank { null },
                                newSourceIsDefault
                            )
                            showAddSourceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Add Source")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSourceDialog = false }) {
                    Text("Cancel", color = PolishTextSecondary)
                }
            }
        )
    }
}
