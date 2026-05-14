package com.example.rentflowmax.data.dao

import androidx.room.*
import com.example.rentflowmax.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE contractId = :contractId ORDER BY paymentDate DESC")
    fun getPaymentsForContract(contractId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE periodMonth = :month AND periodYear = :year ORDER BY paymentDate DESC")
    fun getPaymentsByPeriod(month: Int, year: Int): Flow<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE periodMonth = :month AND periodYear = :year")
    fun getTotalCollectedByPeriod(month: Int, year: Int): Flow<Double>

    @Query("SELECT * FROM payments WHERE id = :id")
    fun getPaymentById(id: Long): Flow<Payment?>

    @Query("SELECT COUNT(*) FROM payments WHERE contractId = :contractId AND periodMonth = :month AND periodYear = :year AND id != :excludeId")
    suspend fun countForContractPeriod(contractId: Long, month: Int, year: Int, excludeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)
}
