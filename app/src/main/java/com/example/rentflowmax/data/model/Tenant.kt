package com.example.rentflowmax.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val idNumber: String,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String get() = "$firstName $lastName"
}
