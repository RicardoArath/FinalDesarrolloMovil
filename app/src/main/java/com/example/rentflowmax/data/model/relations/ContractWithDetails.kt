package com.example.rentflowmax.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.Payment
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.Tenant

data class ContractWithDetails(
    @Embedded val contract: Contract,
    @Relation(parentColumn = "propertyId", entityColumn = "id")
    val property: Property,
    @Relation(parentColumn = "tenantId", entityColumn = "id")
    val tenant: Tenant,
    @Relation(parentColumn = "id", entityColumn = "contractId")
    val payments: List<Payment>
)
