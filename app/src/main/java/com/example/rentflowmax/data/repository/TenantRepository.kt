package com.example.rentflowmax.data.repository

import com.example.rentflowmax.data.dao.TenantDao
import com.example.rentflowmax.data.model.Tenant

class TenantRepository(private val dao: TenantDao) {
    fun getAllTenants() = dao.getAllTenants()
    fun getAllTenantsWithContracts() = dao.getAllTenantsWithContracts()
    fun getTenantById(id: Long) = dao.getTenantById(id)
    fun getTenantWithContractsById(id: Long) = dao.getTenantWithContractsById(id)

    suspend fun insert(tenant: Tenant) = dao.insert(tenant)
    suspend fun update(tenant: Tenant) = dao.update(tenant)
    suspend fun delete(tenant: Tenant) = dao.delete(tenant)
}
