package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PayeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayeeDao {
    @Query("SELECT * FROM payees ORDER BY isDefault DESC, name ASC")
    fun getAllPayees(): Flow<List<PayeeEntity>>

    @Query("SELECT * FROM payees WHERE id = :id LIMIT 1")
    suspend fun getPayeeById(id: String): PayeeEntity?

    @Query("SELECT * FROM payees WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPayee(): PayeeEntity?

    @Query("SELECT COUNT(*) FROM payees")
    suspend fun getPayeeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayee(payee: PayeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayees(payees: List<PayeeEntity>)

    @Update
    suspend fun updatePayee(payee: PayeeEntity)

    @Query("DELETE FROM payees WHERE id = :id")
    suspend fun deletePayee(id: String)

    @Query("UPDATE payees SET isDefault = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setDefaultPayee(id: String)
}
