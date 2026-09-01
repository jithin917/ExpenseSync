package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.TallyExportResult
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyExportScreen(
    transactions: List<TransactionWithDetails>,
    currencySymbol: String = "₹",
    onBackClick: () -> Unit,
    onExportCsv: suspend (List<TransactionWithDetails>) -> TallyExportResult,
    onExportXml: suspend (List<TransactionWithDetails>) -> TallyExportResult
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPreset by remember { mutableStateOf(DateFilterPreset.THIS_MONTH) }
    var selectedFormat by remember { mutableStateOf("CSV") }
    var isExporting by remember { mutableStateOf(false) }

    val tallyDateFormat = remember { SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH) }

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

    Scaffold(
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = { Text("Tally ERP/Prime Export", fontWeight = FontWeight.Bold, color = PolishTextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("export_back_button")
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
            // Tally Integration Hero Banner
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
                                imageVector = Icons.Default.TableChart,
                                contentDescription = "Tally",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tally ERP 9 / TallyPrime Ready",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Strict column mapping for instant voucher creation",
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
                                text = "EXPORT VOUCHERS",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredList.size} Records",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL CLAIM",
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
                        text = "SELECT DATE RANGE",
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

            // Format Selection (CSV vs XML)
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPORT FORMAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // CSV Option
                        Surface(
                            color = if (selectedFormat == "CSV") BrandGreenLight.copy(alpha = 0.4f) else PolishHover,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (selectedFormat == "CSV") 1.5.dp else 1.dp,
                                    color = if (selectedFormat == "CSV") BrandGreen else PolishBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFormat = "CSV" }
                                .padding(12.dp)
                                .testTag("format_csv_option")
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "CSV",
                                        tint = if (selectedFormat == "CSV") BrandGreen else PolishTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tally CSV",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedFormat == "CSV") BrandGreen else PolishTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Standard Excel / CSV Import Utility",
                                    fontSize = 11.sp,
                                    color = PolishTextMuted
                                )
                            }
                        }

                        // XML Option
                        Surface(
                            color = if (selectedFormat == "XML") BrandGreenLight.copy(alpha = 0.4f) else PolishHover,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (selectedFormat == "XML") 1.5.dp else 1.dp,
                                    color = if (selectedFormat == "XML") BrandGreen else PolishBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFormat = "XML" }
                                .padding(12.dp)
                                .testTag("format_xml_option")
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "XML",
                                        tint = if (selectedFormat == "XML") BrandGreen else PolishTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tally XML",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedFormat == "XML") BrandGreen else PolishTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Direct TDL XML Envelope",
                                    fontSize = 11.sp,
                                    color = PolishTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Live Table Preview
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
                            text = "TALLY COLUMN SCHEMA PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${filteredList.size} Vouchers",
                            fontSize = 11.sp,
                            color = BrandGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredList.isEmpty()) {
                        Text(
                            text = "No expenses found for this date range to export.",
                            fontSize = 13.sp,
                            color = PolishTextMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        // Scrollable table
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PolishHover)
                                .horizontalScroll(rememberScrollState())
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier
                                        .background(PolishBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Date", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(90.dp))
                                    Text("Voucher Type", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(95.dp))
                                    Text("Ledger Name", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(140.dp))
                                    Text("Amount", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(85.dp))
                                    Text("Payment Ledger", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(160.dp))
                                    Text("Narration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PolishTextPrimary, modifier = Modifier.width(180.dp))
                                }

                                filteredList.take(10).forEach { item ->
                                    val dateStr = tallyDateFormat.format(Date(item.transaction.transactionDate))
                                    val vType = item.transaction.voucherType
                                    val ledger = item.category?.name ?: "General Expenses"
                                    val amtStr = String.format(Locale.US, "%.2f", item.transaction.amount)
                                    val payment = item.paymentSource?.tallyLedgerName ?: "Cash"
                                    val note = item.transaction.notes ?: item.transaction.merchantName ?: ""

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(dateStr, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = PolishTextPrimary, modifier = Modifier.width(90.dp))
                                        Text(vType, fontSize = 11.sp, color = PolishTextSecondary, modifier = Modifier.width(95.dp))
                                        Text(ledger, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PolishTextPrimary, modifier = Modifier.width(140.dp))
                                        Text(amtStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen, modifier = Modifier.width(85.dp))
                                        Text(payment, fontSize = 11.sp, color = PolishTextSecondary, modifier = Modifier.width(160.dp))
                                        Text(note, fontSize = 11.sp, color = PolishTextMuted, modifier = Modifier.width(180.dp), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Actions
            Button(
                onClick = {
                    if (filteredList.isEmpty()) {
                        Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isExporting = true
                        val result = if (selectedFormat == "CSV") {
                            onExportCsv(filteredList)
                        } else {
                            onExportXml(filteredList)
                        }
                        isExporting = false

                        if (result is TallyExportResult.Success) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (selectedFormat == "CSV") "text/csv" else "text/xml"
                                putExtra(Intent.EXTRA_STREAM, result.uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Tally ${result.format} Export - ${result.recordCount} Vouchers")
                                putExtra(Intent.EXTRA_TEXT, "Attached is the exported Tally ${result.format} file containing ${result.recordCount} vouchers.")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Tally File via..."))
                        } else if (result is TallyExportResult.Error) {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isExporting && filteredList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_and_share_button")
            ) {
                if (isExporting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export & Share $selectedFormat (${filteredList.size} Vouchers)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Quick Copy CSV Button
            OutlinedButton(
                onClick = {
                    if (filteredList.isEmpty()) return@OutlinedButton
                    val builder = StringBuilder()
                    builder.append("Date,Voucher Type,Ledger Name,Amount,Payment Ledger,Narration\n")
                    filteredList.forEach { item ->
                        val d = tallyDateFormat.format(Date(item.transaction.transactionDate))
                        val l = item.category?.name ?: "General"
                        val a = String.format(Locale.US, "%.2f", item.transaction.amount)
                        val p = item.paymentSource?.tallyLedgerName ?: "Cash"
                        val n = item.transaction.notes ?: ""
                        builder.append("$d,Payment,\"$l\",$a,\"$p\",\"$n\"\n")
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Tally CSV", builder.toString()))
                    Toast.makeText(context, "Tally CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_csv_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = PolishTextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy CSV to Clipboard", color = PolishTextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
