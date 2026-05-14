package com.example.rentflowmax.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.Property

data class PropertyWithActiveContract(
    @Embedded val property: Property,
    @Relation(
        parentColumn = "id",
        entityColumn = "propertyId"
    )
    val contracts: List<Contract>
) {
    val activeContract: Contract?
        get() = contracts.firstOrNull { it.isActive && it.endDate >= System.currentTimeMillis() }

    val isRented: Boolean
        get() = activeContract != null
}
