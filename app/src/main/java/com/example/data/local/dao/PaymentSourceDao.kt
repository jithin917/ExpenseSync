package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PaymentSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentSourceDao {
    @Query("SELECT * FROM payment_sources ORDER BY isDefault DESC, bankName ASC")
    fun getAllPaymentSources(): Flow<List<PaymentSourceEntity>>

    @Query("SELECT * FROM payment_sources WHERE id = :id LIMIT 1")
    suspend fun getPaymentSourceById(id: String): PaymentSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSource(source: PaymentSourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSources(sources: List<PaymentSourceEntity>)

    @Update
    suspend fun updatePaymentSource(source: PaymentSourceEntity)

    @Query("DELETE FROM payment_sources WHERE id = :id")
    suspend fun deletePaymentSource(id: String)

    @Query("UPDATE payment_sources SET isDefault = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setDefaultPaymentSource(id: String)

    @Query("SELECT COUNT(*) FROM payment_sources")
    suspend fun getPaymentSourceCount(): Int
}
