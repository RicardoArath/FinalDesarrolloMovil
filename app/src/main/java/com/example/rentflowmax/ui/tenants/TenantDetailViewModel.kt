package com.example.rentflowmax.ui.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.model.relations.TenantWithContracts
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.TenantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TenantDetailViewModel(
    private val tenantId: Long,
    private val tenantRepository: TenantRepository,
    private val contractRepository: ContractRepository
) : ViewModel() {

    val tenant: StateFlow<TenantWithContracts?> =
        tenantRepository.getTenantWithContractsById(tenantId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contracts: StateFlow<List<ContractWithDetails>> =
        contractRepository.getContractsForTenant(tenantId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(tenant: Tenant) = viewModelScope.launch {
        tenantRepository.delete(tenant)
    }
}
