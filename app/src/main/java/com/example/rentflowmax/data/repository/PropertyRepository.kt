package com.example.rentflowmax.data.repository

import com.example.rentflowmax.data.dao.PropertyDao
import com.example.rentflowmax.data.model.Property

class PropertyRepository(private val dao: PropertyDao) {
    fun getAllProperties() = dao.getAllProperties()
    fun getAllPropertiesWithContracts() = dao.getAllPropertiesWithContracts()
    fun getPropertyById(id: Long) = dao.getPropertyById(id)
    fun getPropertyWithContractsById(id: Long) = dao.getPropertyWithContractsById(id)

    suspend fun insert(property: Property) = dao.insert(property)
    suspend fun update(property: Property) = dao.update(property)
    suspend fun delete(property: Property) = dao.delete(property)
}
