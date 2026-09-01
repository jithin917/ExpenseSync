package com.example.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.TransactionWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TallyExportService(private val context: Context) {

    private val exportsDir: File by lazy {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val tallyDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    private val tallyXmlDateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)

    /**
     * Generates a Tally-compatible CSV file based on the PRD specification:
     * Header: Date,Voucher Type,Ledger Name,Amount,Payment Ledger,Narration
     */
    suspend fun generateTallyCsv(
        transactions: List<TransactionWithDetails>,
        employeeName: String
    ): TallyExportResult = withContext(Dispatchers.IO) {
        try {
            val timeStamp = fileTimestampFormat.format(Date())
            val sanitizedName = employeeName.replace("\\s+".toRegex(), "_").ifBlank { "Employee" }
            val fileName = "Expenses_${sanitizedName}_${timeStamp}.csv"
            val file = File(exportsDir, fileName)

            FileWriter(file).use { writer ->
                // CSV Header strictly compliant with Tally Import Utility
                writer.append("Date,Voucher Type,Ledger Name,Amount,Payment Ledger,Narration\n")

                for (item in transactions) {
                    val dateStr = tallyDateFormat.format(Date(item.transaction.transactionDate))
                    val voucherType = escapeCsv(item.transaction.voucherType.ifBlank { "Payment" })
                    val ledgerName = escapeCsv(item.category?.name ?: "General Expenses")
                    val amountStr = String.format(Locale.US, "%.2f", item.transaction.amount)
                    val paymentLedger = escapeCsv(item.paymentSource?.tallyLedgerName ?: "Cash / Bank")
                    
                    // Format narration with payee/merchant name, GST details, and employee notes
                    val payeeName = item.payee?.name ?: item.transaction.merchantName?.takeIf { it.isNotBlank() }
                    val gstNum = item.transaction.gstNumber?.takeIf { it.isNotBlank() } ?: item.payee?.gstNumber?.takeIf { it.isNotBlank() }
                    val gstTag = if (item.transaction.hasGstBill || item.payee?.hasGstBill == true) {
                        if (!gstNum.isNullOrBlank()) "GSTIN: $gstNum" else "GST Bill"
                    } else null
                    val notes = item.transaction.notes?.takeIf { it.isNotBlank() }
                    
                    val parts = listOfNotNull(payeeName, gstTag, notes)
                    val narrationCombined = if (parts.isNotEmpty()) parts.joinToString(" - ") else "Expense reimbursement"
                    val narration = escapeCsv(narrationCombined)

                    writer.append("$dateStr,$voucherType,$ledgerName,$amountStr,$paymentLedger,$narration\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            TallyExportResult.Success(
                file = file,
                uri = uri,
                recordCount = transactions.size,
                format = "CSV"
            )
        } catch (e: Exception) {
            TallyExportResult.Error(e.message ?: "Failed to generate Tally CSV")
        }
    }

    /**
     * Generates standard Tally Prime / ERP 9 XML envelope for direct import.
     */
    suspend fun generateTallyXml(
        transactions: List<TransactionWithDetails>,
        companyName: String = "Company"
    ): TallyExportResult = withContext(Dispatchers.IO) {
        try {
            val timeStamp = fileTimestampFormat.format(Date())
            val fileName = "Tally_Vouchers_${timeStamp}.xml"
            val file = File(exportsDir, fileName)

            FileWriter(file).use { writer ->
                writer.append("""<ENVELOPE>
  <HEADER>
    <TALLYREQUEST>Import Data</TALLYREQUEST>
  </HEADER>
  <BODY>
    <IMPORTDATA>
      <REQUESTDESC>
        <REPORTNAME>Vouchers</REPORTNAME>
        <STATICVARIABLES>
          <SVCURRENTCOMPANY>${escapeXml(companyName)}</SVCURRENTCOMPANY>
        </STATICVARIABLES>
      </REQUESTDESC>
      <REQUESTDATA>
""")

                for (item in transactions) {
                    val dateXml = tallyXmlDateFormat.format(Date(item.transaction.transactionDate))
                    val ledgerName = escapeXml(item.category?.name ?: "General Expenses")
                    val paymentLedger = escapeXml(item.paymentSource?.tallyLedgerName ?: "Cash")
                    val amountStr = String.format(Locale.US, "%.2f", item.transaction.amount)
                    val narration = escapeXml(item.transaction.notes ?: item.transaction.merchantName ?: "Expense")

                    writer.append("""        <TALLYMESSAGE xmlns:UDF="TallyUDF">
          <VOUCHER VCHTYPE="Payment" ACTION="Create">
            <DATE>$dateXml</DATE>
            <NARRATION>$narration</NARRATION>
            <VOUCHERTYPENAME>Payment</VOUCHERTYPENAME>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>$ledgerName</LEDGERNAME>
              <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>
              <AMOUNT>-$amountStr</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>$paymentLedger</LEDGERNAME>
              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>
              <AMOUNT>$amountStr</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
          </VOUCHER>
        </TALLYMESSAGE>
""")
                }

                writer.append("""      </REQUESTDATA>
    </IMPORTDATA>
  </BODY>
</ENVELOPE>""")
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            TallyExportResult.Success(
                file = file,
                uri = uri,
                recordCount = transactions.size,
                format = "XML"
            )
        } catch (e: Exception) {
            TallyExportResult.Error(e.message ?: "Failed to generate Tally XML")
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    private fun escapeXml(value: String): String {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

sealed class TallyExportResult {
    data class Success(
        val file: File,
        val uri: Uri,
        val recordCount: Int,
        val format: String
    ) : TallyExportResult()

    data class Error(val message: String) : TallyExportResult()
}
