package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.TransactionWithDetails
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseSyncService(private val context: Context) {

    private val isFirebaseAvailable: Boolean by lazy {
        try {
            FirebaseApp.initializeApp(context) != null || FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w("FirebaseSyncService", "Firebase not initialized: ${e.message}")
            false
        }
    }

    private val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null

    fun getCurrentUser(): FirebaseUser? {
        return auth?.currentUser
    }

    suspend fun syncTransactionsToCloud(
        userId: String,
        transactions: List<TransactionWithDetails>,
        categories: List<CategoryEntity>,
        paymentSources: List<PaymentSourceEntity>
    ): SyncResult = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            return@withContext SyncResult.Success(
                syncedTransactions = transactions.size,
                isCloudConnected = false,
                message = "Synced locally (${transactions.size} records ready for email & Tally export)"
            )
        }

        try {
            val userDoc = db.collection("users").document(userId)

            // Batch sync transactions
            val batch = db.batch()
            var count = 0

            for (item in transactions) {
                val txnRef = userDoc.collection("expenses").document(item.transaction.id)
                val txnMap = hashMapOf(
                    "id" to item.transaction.id,
                    "amount" to item.transaction.amount,
                    "transactionDate" to item.transaction.transactionDate,
                    "categoryId" to item.transaction.categoryId,
                    "categoryName" to (item.category?.name ?: "General"),
                    "sourceId" to item.transaction.sourceId,
                    "paymentSource" to (item.paymentSource?.displayLabel ?: "Cash"),
                    "notes" to (item.transaction.notes ?: ""),
                    "merchantName" to (item.transaction.merchantName ?: ""),
                    "voucherType" to item.transaction.voucherType,
                    "hasReceipt" to item.receipts.isNotEmpty(),
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(txnRef, txnMap, SetOptions.merge())
                count++
            }

            // Sync categories & sources
            for (cat in categories) {
                val catRef = userDoc.collection("categories").document(cat.id)
                batch.set(catRef, cat, SetOptions.merge())
            }

            for (src in paymentSources) {
                val srcRef = userDoc.collection("payment_sources").document(src.id)
                val srcMap = hashMapOf(
                    "id" to src.id,
                    "type" to src.type,
                    "bankName" to src.bankName,
                    "last4Digits" to (src.last4Digits ?: ""),
                    "displayLabel" to src.displayLabel
                )
                batch.set(srcRef, srcMap, SetOptions.merge())
            }

            batch.commit().await()

            SyncResult.Success(
                syncedTransactions = count,
                isCloudConnected = true,
                message = "Successfully synced $count transactions to Cloud Database!"
            )
        } catch (e: Exception) {
            Log.e("FirebaseSyncService", "Sync error: ${e.message}", e)
            SyncResult.Error("Cloud sync offline: ${e.localizedMessage ?: "Network error"}")
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
    }
}

sealed class SyncResult {
    data class Success(
        val syncedTransactions: Int,
        val isCloudConnected: Boolean,
        val message: String
    ) : SyncResult()

    data class Error(val message: String) : SyncResult()
}
