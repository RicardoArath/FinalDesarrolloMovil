package com.example.rentflowmax.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val type: String,
    val monthlyRent: Double,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
