package com.example.rentflowmax.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(entity = Contract::class, parentColumns = ["id"], childColumns = ["contractId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("contractId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contractId: Long,
    val amount: Double,
    val paymentDate: Long,
    val periodMonth: Int,
    val periodYear: Int,
    val paymentMethod: String,
    val receiptNumber: String = "",
    val notes: String = "",
    val isLate: Boolean = false
)
