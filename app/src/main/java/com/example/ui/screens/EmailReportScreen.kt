package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.EmailReportPackage
import com.example.data.local.entity.TransactionWithDetails
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.viewmodel.DateFilterPreset
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailReportScreen(
    transactions: List<TransactionWithDetails>,
    settingsMap: Map<String, String>,
    currencySymbol: String = "₹",
    onBackClick: () -> Unit,
    onGenerateReport: suspend (List<TransactionWithDetails>, String) -> EmailReportPackage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPreset by remember { mutableStateOf(DateFilterPreset.THIS_MONTH) }
    var isPreparingEmail by remember { mutableStateOf(false) }

    val accountantEmail = settingsMap["accountant_email"] ?: "accounts@company.com"
    val employeeName = settingsMap["employee_name"] ?: "Employee"

    val startOfMonth = remember {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.timeInMillis
    }
    val startOfWeek = remember {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.timeInMillis
    }
    val startOfDay = remember {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.timeInMillis
    }

    val filteredList = remember(transactions, selectedPreset) {
        when (selectedPreset) {
            DateFilterPreset.ALL -> transactions
            DateFilterPreset.TODAY -> transactions.filter { it.transaction.transactionDate >= startOfDay }
            DateFilterPreset.THIS_WEEK -> transactions.filter { it.transaction.transactionDate >= startOfWeek }
            DateFilterPreset.THIS_MONTH -> transactions.filter { it.transaction.transactionDate >= startOfMonth }
            DateFilterPreset.CUSTOM -> transactions
        }
    }

    val totalAmount = filteredList.sumOf { it.transaction.amount }
    val totalReceipts = filteredList.sumOf { it.receipts.size }

    Scaffold(
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = { Text("Email Expense Report", fontWeight = FontWeight.Bold, color = PolishTextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("email_back_button")
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
            // Header Dispatch Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ForwardToInbox,
                                contentDescription = "Email",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accounts Dispatcher",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Recipient: $accountantEmail",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "EXPENSES CLAIM",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatCurrency(totalAmount, currencySymbol),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ATTACHMENTS",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tally CSV + $totalReceipts Bills",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Date Range Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REPORTING PERIOD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = { Text(preset.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreenLight,
                                    selectedLabelColor = BrandGreenDark,
                                    containerColor = PolishSurface,
                                    labelColor = PolishTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedPreset == preset) BrandGreen else PolishBorder,
                                    selectedBorderColor = BrandGreen,
                                    enabled = true,
                                    selected = selectedPreset == preset
                                )
                            )
                        }
                    }
                }
            }

            // Summary Breakdown Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPENSE HEAD SUMMARY BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val categoryGroups = filteredList
                        .groupBy { it.category?.name ?: "General Expenses" }
                        .mapValues { (_, txns) -> txns.sumOf { it.transaction.amount } }
                        .toList()
                        .sortedByDescending { it.second }

                    if (categoryGroups.isEmpty()) {
                        Text(
                            text = "No expenses recorded in this period.",
                            color = PolishTextMuted,
                            fontSize = 13.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for ((catName, amt) in categoryGroups) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = catName,
                                        fontSize = 13.sp,
                                        color = PolishTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formatCurrency(amt, currencySymbol),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    if (filteredList.isEmpty()) {
                        Toast.makeText(context, "No expenses to report for this period", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isPreparingEmail = true
                        val reportPackage = onGenerateReport(filteredList, selectedPreset.label)
                        isPreparingEmail = false

                        try {
                            context.startActivity(Intent.createChooser(reportPackage.sendIntent, "Send Expense Report via..."))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not launch email app: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isPreparingEmail && filteredList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_email_report_button")
            ) {
                if (isPreparingEmail) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Report to $accountantEmail",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    if (filteredList.isEmpty()) return@OutlinedButton
                    scope.launch {
                        val reportPackage = onGenerateReport(filteredList, selectedPreset.label)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Expense Report", reportPackage.body))
                        Toast.makeText(context, "Full expense report text copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_report_text_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = PolishTextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Report Text to Clipboard", color = PolishTextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
