package com.example.rentflowmax.data.repository

import com.example.rentflowmax.data.dao.ContractDao
import com.example.rentflowmax.data.model.Contract

class ContractRepository(private val dao: ContractDao) {
    fun getAllContractsWithDetails() = dao.getAllContractsWithDetails()
    fun getActiveContractsWithDetails() = dao.getActiveContractsWithDetails()
    fun getContractsForProperty(propertyId: Long) = dao.getContractsForProperty(propertyId)
    fun getActiveContractForProperty(propertyId: Long) = dao.getActiveContractForProperty(propertyId)
    fun getContractsForTenant(tenantId: Long) = dao.getContractsForTenant(tenantId)
    fun getContractWithDetailsById(id: Long) = dao.getContractWithDetailsById(id)
    fun getActiveContractCount() = dao.getActiveContractCount()
    fun getContractsExpiringSoonCount(now: Long, limit: Long) = dao.getContractsExpiringSoonCount(now, limit)

    suspend fun insert(contract: Contract): Long = dao.insert(contract)
    suspend fun update(contract: Contract) = dao.update(contract)
    suspend fun delete(contract: Contract) = dao.delete(contract)

    suspend fun countActiveForProperty(propertyId: Long, excludeContractId: Long = -1L): Int =
        dao.countActiveContractsForProperty(propertyId, excludeContractId)
}
