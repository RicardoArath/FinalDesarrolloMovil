package com.example.rentflowmax.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contracts",
    foreignKeys = [
        ForeignKey(entity = Property::class, parentColumns = ["id"], childColumns = ["propertyId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = Tenant::class, parentColumns = ["id"], childColumns = ["tenantId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("propertyId"), Index("tenantId")]
)
data class Contract(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val tenantId: Long,
    val startDate: Long,
    val endDate: Long,
    val monthlyRent: Double,
    val depositAmount: Double = 0.0,
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
