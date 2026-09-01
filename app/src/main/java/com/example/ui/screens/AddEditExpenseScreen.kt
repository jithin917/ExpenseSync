package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.ui.components.formatExpenseDateTime
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenTint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.viewmodel.ExpenseFormState
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    formState: ExpenseFormState,
    categories: List<CategoryEntity>,
    paymentSources: List<PaymentSourceEntity>,
    payees: List<PayeeEntity> = emptyList(),
    currencySymbol: String = "₹",
    onAmountChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onPayeeSelect: (PayeeEntity?) -> Unit,
    onMerchantChange: (String) -> Unit,
    onHasGstBillChange: (Boolean) -> Unit,
    onGstNumberChange: (String) -> Unit,
    onQuickAddPayee: (
        name: String,
        categoryTag: String?,
        defaultCategoryId: String?,
        defaultPaymentSourceId: String?,
        defaultPaymentMode: String?,
        hasGst: Boolean,
        gstNum: String?
    ) -> Unit,
    onNotesChange: (String) -> Unit,
    onReceiptChange: (Uri?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    createTempCameraUri: () -> Uri
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showQuickAddPayeeDialog by remember { mutableStateOf(false) }

    // Quick Add Payee State
    var quickPayeeName by remember { mutableStateOf("") }
    var quickPayeeTag by remember { mutableStateOf("") }
    var quickPayeeCatId by remember { mutableStateOf<String?>(null) }
    var quickPayeeMode by remember { mutableStateOf("Credit Card (CC)") }
    var quickPayeeSourceId by remember { mutableStateOf<String?>(null) }
    var quickPayeeHasGst by remember { mutableStateOf(false) }
    var quickPayeeGstNum by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onReceiptChange(tempCameraUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onReceiptChange(uri)
        }
    }

    Scaffold(
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.id == null) "Log New Expense" else "Edit Expense",
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancelClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
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
            // Receipt capture card (Top prominence for 100% receipt capture compliance)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (formState.receiptImageUri != null || formState.existingReceiptUrl != null) {
                        PolishSurface
                    } else {
                        BrandGreenTint
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                    .testTag("receipt_capture_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Receipt",
                            tint = BrandGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Receipt / Bill Attachment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PolishTextPrimary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (formState.receiptImageUri != null || formState.existingReceiptUrl != null) {
                            Surface(
                                color = BrandGreenTint,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Attached",
                                    color = BrandGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (formState.receiptImageUri != null || formState.existingReceiptUrl != null) {
                        val imageModel = formState.receiptImageUri ?: formState.existingReceiptUrl?.let { File(it) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Receipt Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            val uri = createTempCameraUri()
                                            tempCameraUri = uri
                                            cameraLauncher.launch(uri)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Retake",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = ExpenseRed.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { onReceiptChange(null) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val uri = createTempCameraUri()
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandGreenLight,
                                    contentColor = BrandGreenDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("snap_bill_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = BrandGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Snap Bill", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandGreenDark)
                            }

                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_gallery_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Gallery",
                                    tint = PolishTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", fontSize = 13.sp, color = PolishTextSecondary)
                            }
                        }
                    }
                }
            }

            // Amount Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPENSE AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currencySymbol,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = formState.amountText,
                            onValueChange = onAmountChange,
                            placeholder = { Text("0.00", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = PolishTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("amount_input")
                        )
                    }

                    if (formState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formState.errorMessage,
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Payee / Vendor Selector Card (With 1-Tap quick selection and presets)
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
                        Text(
                            text = "PAYEE / VENDOR (RECURRING / DEFAULT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextMuted,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                quickPayeeName = ""
                                quickPayeeTag = ""
                                quickPayeeCatId = categories.firstOrNull()?.id
                                quickPayeeMode = "Credit Card (CC)"
                                quickPayeeSourceId = paymentSources.firstOrNull { it.type == "CC" }?.id
                                quickPayeeHasGst = false
                                quickPayeeGstNum = ""
                                showQuickAddPayeeDialog = true
                            },
                            modifier = Modifier.testTag("quick_add_payee_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New Vendor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Flow of frequent/configured Payees
                    if (payees.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (payee in payees) {
                                val isSelected = formState.payeeId == payee.id
                                val paymentModeBadge = when {
                                    payee.defaultPaymentMode?.contains("CC", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("Credit", ignoreCase = true) == true ||
                                    payee.categoryTag.equals("Fabric", ignoreCase = true) ||
                                    payee.categoryTag.equals("Lining", ignoreCase = true) ||
                                    payee.categoryTag.equals("Accessories", ignoreCase = true) -> "💳 CC"
                                    payee.defaultPaymentMode?.contains("UPI", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("GPAY", ignoreCase = true) == true ||
                                    payee.defaultPaymentMode?.contains("Cash", ignoreCase = true) == true ||
                                    payee.categoryTag.equals("Rent", ignoreCase = true) ||
                                    payee.name.contains("Rent", ignoreCase = true) -> "📱 GPAY/Cash"
                                    payee.defaultPaymentMode?.contains("SB", ignoreCase = true) == true -> "🏦 SB"
                                    else -> null
                                }

                                Surface(
                                    color = if (isSelected) BrandGreen else PolishHover,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clickable {
                                            if (isSelected) {
                                                onPayeeSelect(null)
                                            } else {
                                                onPayeeSelect(payee)
                                            }
                                        }
                                        .testTag("payee_chip_${payee.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = payee.name,
                                            color = if (isSelected) Color.White else PolishTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )

                                        if (!payee.categoryTag.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else BrandGreenTint,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = payee.categoryTag,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else BrandGreen,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        if (payee.defaultAmount != null && payee.defaultAmount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE0F2FE),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                val amtDisplay = if (payee.defaultAmount % 1.0 == 0.0) "$currencySymbol${payee.defaultAmount.toLong()}" else "$currencySymbol%.2f".format(payee.defaultAmount)
                                                Text(
                                                    text = amtDisplay,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF0369A1),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        if (paymentModeBadge != null) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF3E8FF),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = paymentModeBadge,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF7C3AED),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        if (payee.hasGstBill) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE0F2FE),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "GST",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF0284C7),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Merchant text field
                    OutlinedTextField(
                        value = formState.merchantName,
                        onValueChange = onMerchantChange,
                        label = { Text("Vendor / Merchant Name") },
                        placeholder = { Text("e.g. Radhakrishna Textiles, Olive Fashion, Rent") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Store, contentDescription = "Merchant", tint = BrandGreen)
                        },
                        trailingIcon = {
                            if (formState.merchantName.isNotBlank()) {
                                IconButton(onClick = { onMerchantChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = PolishTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("merchant_input")
                    )
                }
            }

            // GST Bill & Tax Details Card
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
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "GST",
                            tint = if (formState.hasGstBill) BrandGreen else PolishTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GST Bill / Tax Invoice Provided",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = "Enable to record vendor GSTIN & claim Input Tax Credit",
                                fontSize = 11.sp,
                                color = PolishTextMuted
                            )
                        }
                        Switch(
                            checked = formState.hasGstBill,
                            onCheckedChange = onHasGstBillChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandGreen
                            ),
                            modifier = Modifier.testTag("gst_bill_switch")
                        )
                    }

                    if (formState.hasGstBill) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = formState.gstNumber,
                            onValueChange = onGstNumberChange,
                            label = { Text("Vendor GSTIN (15 Digits)") },
                            placeholder = { Text("e.g. 32AAAAA0000A1Z5") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen,
                                unfocusedBorderColor = PolishBorder
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gst_number_input")
                        )
                    }
                }
            }

            // Date & Time Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = formState.transactionDate }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            newCal.set(Calendar.MINUTE, minute)
                                            onDateChange(newCal.timeInMillis)
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(16.dp)
                        .testTag("date_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = BrandGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Date & Time",
                            fontSize = 12.sp,
                            color = PolishTextMuted
                        )
                        Text(
                            text = formatExpenseDateTime(formState.transactionDate),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PolishTextPrimary
                        )
                    }
                }
            }

            // Expense Head / Category Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPENSE HEAD / CATEGORY (TALLY LEDGER)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (cat in categories) {
                            val isSelected = formState.categoryId == cat.id
                            Surface(
                                color = if (isSelected) BrandGreen else PolishHover,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clickable { onCategoryChange(cat.id) }
                                    .testTag("category_chip_${cat.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = cat.name,
                                        color = if (isSelected) Color.White else PolishTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Source Selector (SB / CC)
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PAYMENT SOURCE (BANK / CREDIT CARD)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (src in paymentSources) {
                            val isSelected = formState.sourceId == src.id
                            Surface(
                                color = if (isSelected) BrandGreenLight.copy(alpha = 0.4f) else PolishSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BrandGreen else PolishBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSourceChange(src.id) }
                                    .testTag("payment_source_item_${src.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (src.type == "CC") Icons.Default.CreditCard else Icons.Default.Payment,
                                        contentDescription = src.type,
                                        tint = if (isSelected) BrandGreen else PolishTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = src.displayLabel,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) BrandGreen else PolishTextPrimary
                                        )
                                        Text(
                                            text = if (src.type == "CC") "Company Credit Card" else "Savings Bank / Direct Debit",
                                            fontSize = 11.sp,
                                            color = PolishTextMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Context Narration
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Narration / Context Notes (Optional)") },
                        placeholder = { Text("e.g. Lining fabric procurement for Batch #402, monthly studio rent") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedBorderColor = PolishBorder
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input")
                    )
                }
            }

            // Save Action Button
            Button(
                onClick = onSaveClick,
                enabled = !formState.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_expense_button")
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (formState.id == null) "Save & Log Expense" else "Update Expense",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Quick Add Payee / Vendor Dialog
    if (showQuickAddPayeeDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddPayeeDialog = false },
            title = { Text("Configure Vendor / Payee", fontWeight = FontWeight.Bold, color = PolishTextPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quickPayeeName,
                        onValueChange = { quickPayeeName = it },
                        label = { Text("Vendor / Payee Name *") },
                        placeholder = { Text("e.g. Olive Fashion, Parag Fashion") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quickPayeeTag,
                        onValueChange = { quickPayeeTag = it },
                        label = { Text("Item / Material Tag (e.g. Lining, Fabric, Rent)") },
                        placeholder = { Text("e.g. Lining, Fabric, Accessories, Rent") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

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
                                val isSelected = quickPayeeMode == modeKey
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
                                            quickPayeeMode = modeKey
                                            quickPayeeSourceId = when (modeKey) {
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
                                    val isCatSelected = quickPayeeCatId == cat.id
                                    Surface(
                                        color = if (isCatSelected) BrandGreen else PolishHover,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { quickPayeeCatId = cat.id }
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
                            checked = quickPayeeHasGst,
                            onCheckedChange = { quickPayeeHasGst = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandGreen)
                        )
                    }

                    if (quickPayeeHasGst) {
                        OutlinedTextField(
                            value = quickPayeeGstNum,
                            onValueChange = { quickPayeeGstNum = it.uppercase() },
                            label = { Text("GSTIN Number") },
                            placeholder = { Text("e.g. 32AAAAA0000A1Z5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickPayeeName.isNotBlank()) {
                            onQuickAddPayee(
                                quickPayeeName.trim(),
                                quickPayeeTag.trim().ifBlank { null },
                                quickPayeeCatId,
                                quickPayeeSourceId,
                                quickPayeeMode,
                                quickPayeeHasGst,
                                quickPayeeGstNum.trim().ifBlank { null }
                            )
                            showQuickAddPayeeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddPayeeDialog = false }) {
                    Text("Cancel", color = PolishTextSecondary)
                }
            }
        )
    }
}

