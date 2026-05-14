package com.example.rentflowmax.data.dao

import androidx.room.*
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.relations.PropertyWithActiveContract
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY name ASC")
    fun getAllProperties(): Flow<List<Property>>

    @Transaction
    @Query("SELECT * FROM properties ORDER BY name ASC")
    fun getAllPropertiesWithContracts(): Flow<List<PropertyWithActiveContract>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<Property?>

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyWithContractsById(id: Long): Flow<PropertyWithActiveContract?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(property: Property): Long

    @Update
    suspend fun update(property: Property)

    @Delete
    suspend fun delete(property: Property)
}
