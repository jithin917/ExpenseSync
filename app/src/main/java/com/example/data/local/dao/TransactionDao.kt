package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionWithDetailsById(id: String): TransactionWithDetails?

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Transaction
    @Query("SELECT * FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate ORDER BY transactionDate DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate ORDER BY transactionDate DESC")
    suspend fun getTransactionsBetweenDatesOnce(startDate: Long, endDate: Long): List<TransactionWithDetails>

    @Transaction
    @Query("SELECT * FROM transactions WHERE syncStatus = :syncStatus ORDER BY transactionDate DESC")
    fun getTransactionsBySyncStatus(syncStatus: String): Flow<List<TransactionWithDetails>>

    @Query("SELECT COUNT(*) FROM transactions WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET syncStatus = 'SYNCED', updatedAt = :updatedAt WHERE syncStatus = 'PENDING'")
    suspend fun markAllAsSynced(updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE transactionDate >= :startDate AND transactionDate <= :endDate")
    fun getTotalAmountBetweenDates(startDate: Long, endDate: Long): Flow<Double?>
}
