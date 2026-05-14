package com.example.rentflowmax.data.dao

import androidx.room.*
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.data.model.relations.TenantWithContracts
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants ORDER BY firstName ASC, lastName ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Transaction
    @Query("SELECT * FROM tenants ORDER BY firstName ASC, lastName ASC")
    fun getAllTenantsWithContracts(): Flow<List<TenantWithContracts>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantById(id: Long): Flow<Tenant?>

    @Transaction
    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantWithContractsById(id: Long): Flow<TenantWithContracts?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tenant: Tenant): Long

    @Update
    suspend fun update(tenant: Tenant)

    @Delete
    suspend fun delete(tenant: Tenant)
}
