package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.TransactionWithDetails
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmailReportService(private val context: Context) {

    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    /**
     * Builds structured Email Report content and creates an Android Intent
     * configured for the Accountant email ID.
     */
    fun createEmailReport(
        transactions: List<TransactionWithDetails>,
        employeeName: String,
        employeeId: String,
        companyName: String,
        accountantEmail: String,
        currencySymbol: String = "₹",
        dateRangeLabel: String,
        csvFile: File? = null
    ): EmailReportPackage {
        val totalAmount = transactions.sumOf { it.transaction.amount }
        val categoryBreakdown = transactions
            .groupBy { it.category?.name ?: "General Expenses" }
            .mapValues { (_, txns) -> txns.sumOf { it.transaction.amount } }
            .toList()
            .sortedByDescending { it.second }

        val paymentSourceBreakdown = transactions
            .groupBy { it.paymentSource?.displayLabel ?: "Direct Cash / Unassigned" }
            .mapValues { (_, txns) -> txns.sumOf { it.transaction.amount } }
            .toList()
            .sortedByDescending { it.second }

        val subject = "Expense Report - $employeeName ($employeeId) - $dateRangeLabel"

        // Build Plain Text & Markdown Body
        val bodyBuilder = StringBuilder()
        bodyBuilder.append("Dear Accounts Team,\n\n")
        bodyBuilder.append("Please find below the expense report summary submitted for reimbursement / Tally ledger accounting.\n\n")
        bodyBuilder.append("================================================\n")
        bodyBuilder.append("EXPENSE REPORT SUMMARY\n")
        bodyBuilder.append("================================================\n")
        bodyBuilder.append("Employee: $employeeName (ID: $employeeId)\n")
        if (companyName.isNotBlank()) {
            bodyBuilder.append("Company: $companyName\n")
        }
        bodyBuilder.append("Period: $dateRangeLabel\n")
        bodyBuilder.append("Total Transactions: ${transactions.size}\n")
        bodyBuilder.append("TOTAL CLAIM AMOUNT: $currencySymbol ${String.format(Locale.US, "%,.2f", totalAmount)}\n\n")

        bodyBuilder.append("--- CATEGORY BREAKDOWN ---\n")
        for ((cat, amt) in categoryBreakdown) {
            val percentage = if (totalAmount > 0) (amt / totalAmount) * 100 else 0.0
            bodyBuilder.append("• $cat: $currencySymbol ${String.format(Locale.US, "%,.2f", amt)} (${String.format(Locale.US, "%.1f", percentage)}%)\n")
        }
        bodyBuilder.append("\n")

        bodyBuilder.append("--- PAYMENT SOURCE BREAKDOWN ---\n")
        for ((src, amt) in paymentSourceBreakdown) {
            bodyBuilder.append("• $src: $currencySymbol ${String.format(Locale.US, "%,.2f", amt)}\n")
        }
        bodyBuilder.append("\n")

        bodyBuilder.append("--- TRANSACTION LINE ITEMS ---\n")
        transactions.forEachIndexed { index, item ->
            val dateStr = displayDateFormat.format(Date(item.transaction.transactionDate))
            val cat = item.category?.name ?: "General"
            val amt = "$currencySymbol ${String.format(Locale.US, "%,.2f", item.transaction.amount)}"
            val src = item.paymentSource?.displayLabel ?: "Cash"
            val payeeName = item.payee?.name ?: item.transaction.merchantName
            val vendorTag = if (!payeeName.isNullOrBlank()) " [Vendor: $payeeName]" else ""
            val gstNum = item.transaction.gstNumber?.takeIf { it.isNotBlank() } ?: item.payee?.gstNumber?.takeIf { it.isNotBlank() }
            val gstInfo = if (item.transaction.hasGstBill || item.payee?.hasGstBill == true) {
                if (!gstNum.isNullOrBlank()) " (GSTIN: $gstNum)" else " (GST Bill)"
            } else ""
            val note = item.transaction.notes?.let { " - $it" } ?: ""
            val receiptTag = if (item.receipts.isNotEmpty()) " [📎 Receipt Attached]" else ""

            bodyBuilder.append("${index + 1}. $dateStr | $amt | $cat$vendorTag$gstInfo | Paid via: $src$note$receiptTag\n")
        }

        bodyBuilder.append("\n================================================\n")
        bodyBuilder.append("Attached: Tally-compatible import CSV file.\n")
        bodyBuilder.append("Generated seamlessly via ExpenseSync Mobile.\n")

        val bodyText = bodyBuilder.toString()

        // Prepare Attachments
        val attachmentUris = ArrayList<Uri>()
        if (csvFile != null && csvFile.exists()) {
            val csvUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            attachmentUris.add(csvUri)
        }

        // Attach receipt images if present
        for (txn in transactions) {
            for (receipt in txn.receipts) {
                val file = File(receipt.localFilePath)
                if (file.exists()) {
                    try {
                        val receiptUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        if (!attachmentUris.contains(receiptUri)) {
                            attachmentUris.add(receiptUri)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        val sendIntent = if (attachmentUris.size > 1) {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(accountantEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else if (attachmentUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(accountantEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
                putExtra(Intent.EXTRA_STREAM, attachmentUris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(accountantEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
            }
        }

        return EmailReportPackage(
            subject = subject,
            body = bodyText,
            recipientEmail = accountantEmail,
            totalAmount = totalAmount,
            transactionCount = transactions.size,
            attachmentsCount = attachmentUris.size,
            sendIntent = sendIntent
        )
    }
}

data class EmailReportPackage(
    val subject: String,
    val body: String,
    val recipientEmail: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val attachmentsCount: Int,
    val sendIntent: Intent
)
