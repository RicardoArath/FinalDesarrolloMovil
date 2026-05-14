package com.example.rentflowmax.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_requests",
    foreignKeys = [
        ForeignKey(entity = Property::class, parentColumns = ["id"], childColumns = ["propertyId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("propertyId")]
)
data class Maintenance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val reportedDate: Long = System.currentTimeMillis(),
    val resolvedDate: Long? = null,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val technicianName: String = "",
    val notes: String = ""
) {
    companion object {
        const val STATUS_PENDING = "Pendiente"
        const val STATUS_IN_PROGRESS = "En Progreso"
        const val STATUS_COMPLETED = "Completado"

        const val PRIORITY_LOW = "Baja"
        const val PRIORITY_MEDIUM = "Media"
        const val PRIORITY_HIGH = "Alta"
        const val PRIORITY_URGENT = "Urgente"
    }
}
