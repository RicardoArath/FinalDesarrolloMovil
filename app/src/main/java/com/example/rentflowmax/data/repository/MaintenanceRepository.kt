package com.example.rentflowmax.data.repository

import com.example.rentflowmax.data.dao.MaintenanceDao
import com.example.rentflowmax.data.model.Maintenance

class MaintenanceRepository(private val dao: MaintenanceDao) {
    fun getAllMaintenance() = dao.getAllMaintenance()
    fun getMaintenanceByStatus(status: String) = dao.getMaintenanceByStatus(status)
    fun getMaintenanceForProperty(propertyId: Long) = dao.getMaintenanceForProperty(propertyId)
    fun getMaintenanceById(id: Long) = dao.getMaintenanceById(id)
    fun getPendingMaintenanceCount() = dao.getPendingMaintenanceCount()

    suspend fun insert(maintenance: Maintenance) = dao.insert(maintenance)
    suspend fun update(maintenance: Maintenance) = dao.update(maintenance)
    suspend fun delete(maintenance: Maintenance) = dao.delete(maintenance)
}
