package com.example.rentflowmax.data.dao

import androidx.room.*
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {
    @Transaction
    @Query("SELECT * FROM contracts ORDER BY startDate DESC")
    fun getAllContractsWithDetails(): Flow<List<ContractWithDetails>>

    @Transaction
    @Query("SELECT * FROM contracts WHERE isActive = 1 AND endDate >= :now ORDER BY endDate ASC")
    fun getActiveContractsWithDetails(now: Long = System.currentTimeMillis()): Flow<List<ContractWithDetails>>

    @Transaction
    @Query("SELECT * FROM contracts WHERE propertyId = :propertyId ORDER BY startDate DESC")
    fun getContractsForProperty(propertyId: Long): Flow<List<ContractWithDetails>>

    @Transaction
    @Query("SELECT * FROM contracts WHERE propertyId = :propertyId AND isActive = 1 AND endDate >= :now LIMIT 1")
    fun getActiveContractForProperty(propertyId: Long, now: Long = System.currentTimeMillis()): Flow<ContractWithDetails?>

    @Transaction
    @Query("SELECT * FROM contracts WHERE tenantId = :tenantId ORDER BY startDate DESC")
    fun getContractsForTenant(tenantId: Long): Flow<List<ContractWithDetails>>

    @Transaction
    @Query("SELECT * FROM contracts WHERE id = :id")
    fun getContractWithDetailsById(id: Long): Flow<ContractWithDetails?>

    @Query("SELECT COUNT(*) FROM contracts WHERE isActive = 1 AND endDate >= :now")
    fun getActiveContractCount(now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT COUNT(*) FROM contracts WHERE isActive = 1 AND endDate BETWEEN :now AND :limit")
    fun getContractsExpiringSoonCount(now: Long, limit: Long): Flow<Int>

    @Query("UPDATE contracts SET isActive = 0 WHERE propertyId = :propertyId AND id != :excludeId")
    suspend fun deactivateOtherContractsForProperty(propertyId: Long, excludeId: Long)

    @Query("SELECT COUNT(*) FROM contracts WHERE propertyId = :propertyId AND isActive = 1 AND endDate >= :now AND id != :excludeId")
    suspend fun countActiveContractsForProperty(propertyId: Long, excludeId: Long, now: Long = System.currentTimeMillis()): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: Contract): Long

    @Update
    suspend fun update(contract: Contract)

    @Delete
    suspend fun delete(contract: Contract)
}
