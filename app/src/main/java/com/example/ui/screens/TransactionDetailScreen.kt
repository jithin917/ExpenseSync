package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.TransactionWithDetails
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.PaymentSourcePill
import com.example.ui.components.ReceiptViewerDialog
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatExpenseDateTime
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionWithDetails: TransactionWithDetails,
    auditLogs: List<AuditLogEntity>,
    currencySymbol: String = "₹",
    onBackClick: () -> Unit,
    onEditClick: (TransactionWithDetails) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var viewingReceiptPath by remember { mutableStateOf<String?>(null) }

    val txn = transactionWithDetails.transaction
    val category = transactionWithDetails.category
    val source = transactionWithDetails.paymentSource
    val receipts = transactionWithDetails.receipts

    Scaffold(
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = { Text("Expense Details", fontWeight = FontWeight.Bold, color = PolishTextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("detail_back_button")
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
                        onClick = { onEditClick(transactionWithDetails) },
                        modifier = Modifier.testTag("edit_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BrandGreen
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed
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
            // Header Amount Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL CLAIM AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatCurrency(txn.amount, currencySymbol),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SyncStatusBadge(status = txn.syncStatus)
                        if (source != null) {
                            PaymentSourcePill(type = source.type, displayLabel = source.displayLabel)
                        }
                    }
                }
            }

            // Attached Receipt
            if (receipts.isNotEmpty()) {
                val firstReceipt = receipts.first()
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
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Receipt",
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attached Bill / Receipt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { viewingReceiptPath = firstReceipt.localFilePath },
                                modifier = Modifier.testTag("view_full_receipt_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Zoom",
                                    tint = BrandGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Full View", color = BrandGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val file = File(firstReceipt.localFilePath)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.05f))
                                .clickable { viewingReceiptPath = firstReceipt.localFilePath },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Receipt Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Meta Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIconBadge(
                            iconName = category?.iconName ?: "category",
                            colorHex = category?.colorHex ?: "#386B3B"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Expense Head / Category", fontSize = 12.sp, color = PolishTextMuted)
                            Text(
                                text = category?.name ?: "General Expenses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PolishTextPrimary
                            )
                        }
                    }

                    // Merchant / Payee
                    if (!txn.merchantName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PolishHover)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "Merchant",
                                    tint = PolishTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Merchant / Vendor / Payee", fontSize = 12.sp, color = PolishTextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = txn.merchantName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PolishTextPrimary)
                                    if (transactionWithDetails.payee?.categoryTag != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = BrandGreenLight,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = transactionWithDetails.payee.categoryTag,
                                                color = BrandGreenDark,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // GST Status & GSTIN
                    if (txn.hasGstBill || !txn.gstNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE))
                            ) {
                                Text(
                                    text = "GST",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0369A1)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Tax & GST Invoice Status", fontSize = 12.sp, color = PolishTextMuted)
                                Text(
                                    text = if (!txn.gstNumber.isNullOrBlank()) "GST Bill Provided (GSTIN: ${txn.gstNumber})" else "GST Bill Provided",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }

                    // Date & Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PolishHover)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = PolishTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Date & Time Logged", fontSize = 12.sp, color = PolishTextMuted)
                            Text(
                                text = formatExpenseDateTime(txn.transactionDate),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PolishTextPrimary
                            )
                        }
                    }

                    // Payment Source
                    if (source != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PolishHover)
                            ) {
                                Icon(
                                    imageVector = if (source.type == "CC") Icons.Default.CreditCard else Icons.Default.Payment,
                                    contentDescription = "Payment Source",
                                    tint = PolishTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Payment Ledger", fontSize = 12.sp, color = PolishTextMuted)
                                Text(text = source.displayLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PolishTextPrimary)
                            }
                        }
                    }

                    // Narration / Notes
                    if (!txn.notes.isNullOrBlank()) {
                        Column {
                            Text(text = "Narration / Context Notes", fontSize = 12.sp, color = PolishTextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = PolishHover,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = txn.notes,
                                    fontSize = 13.sp,
                                    color = PolishTextPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Audit Trail
            val relevantLogs = auditLogs.filter { it.transactionId == txn.id }
            if (relevantLogs.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = BrandGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audit Trail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PolishTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        for (log in relevantLogs) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${log.details}",
                                    fontSize = 12.sp,
                                    color = PolishTextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatExpenseDateTime(log.timestamp),
                                    fontSize = 11.sp,
                                    color = PolishTextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Receipt Full Screen Dialog
    viewingReceiptPath?.let { path ->
        ReceiptViewerDialog(
            imagePath = path,
            onDismiss = { viewingReceiptPath = null }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense Record?") },
            text = { Text("Are you sure you want to delete this ${formatCurrency(txn.amount, currencySymbol)} expense? This will also remove any local receipt image.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick(txn.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

