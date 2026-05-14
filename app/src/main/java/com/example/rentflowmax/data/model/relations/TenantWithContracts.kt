package com.example.rentflowmax.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.Tenant

data class TenantWithContracts(
    @Embedded val tenant: Tenant,
    @Relation(parentColumn = "id", entityColumn = "tenantId")
    val contracts: List<Contract>
) {
    val hasActiveContract: Boolean
        get() = contracts.any { it.isActive && it.endDate >= System.currentTimeMillis() }
}
