package com.example.rentflowmax.ui.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.data.model.relations.TenantWithContracts
import com.example.rentflowmax.data.repository.TenantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TenantListViewModel(private val repository: TenantRepository) : ViewModel() {

    val tenants: StateFlow<List<TenantWithContracts>> =
        repository.getAllTenantsWithContracts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(tenant: Tenant) = viewModelScope.launch {
        repository.delete(tenant)
    }
}
