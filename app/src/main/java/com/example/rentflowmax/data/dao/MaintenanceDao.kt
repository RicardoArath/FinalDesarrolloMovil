package com.example.rentflowmax.data.dao

import androidx.room.*
import com.example.rentflowmax.data.model.Maintenance
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_requests ORDER BY reportedDate DESC")
    fun getAllMaintenance(): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenance_requests WHERE status = :status ORDER BY reportedDate DESC")
    fun getMaintenanceByStatus(status: String): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenance_requests WHERE propertyId = :propertyId ORDER BY reportedDate DESC")
    fun getMaintenanceForProperty(propertyId: Long): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenance_requests WHERE id = :id")
    fun getMaintenanceById(id: Long): Flow<Maintenance?>

    @Query("SELECT COUNT(*) FROM maintenance_requests WHERE status != 'Completado'")
    fun getPendingMaintenanceCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenance: Maintenance): Long

    @Update
    suspend fun update(maintenance: Maintenance)

    @Delete
    suspend fun delete(maintenance: Maintenance)
}
