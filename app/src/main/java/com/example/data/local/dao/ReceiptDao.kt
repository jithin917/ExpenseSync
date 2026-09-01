package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts WHERE transactionId = :transactionId")
    fun getReceiptsForTransaction(transactionId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE transactionId = :transactionId")
    suspend fun getReceiptsForTransactionOnce(transactionId: String): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE syncStatus = 'PENDING'")
    suspend fun getPendingReceipts(): List<ReceiptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Query("UPDATE receipts SET syncStatus = :syncStatus, cloudUrl = :cloudUrl WHERE id = :id")
    suspend fun updateReceiptSync(id: String, syncStatus: String, cloudUrl: String?)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: String)

    @Query("DELETE FROM receipts WHERE transactionId = :transactionId")
    suspend fun deleteReceiptsForTransaction(transactionId: String)
}
