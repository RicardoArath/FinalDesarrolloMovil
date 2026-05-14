package com.example.rentflowmax.ui.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import com.example.rentflowmax.data.repository.TenantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContractFormViewModel(
    private val contractId: Long,
    private val contractRepository: ContractRepository,
    private val propertyRepository: PropertyRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    val contract: StateFlow<ContractWithDetails?> =
        if (contractId != -1L) contractRepository.getContractWithDetailsById(contractId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        else MutableStateFlow(null)

    val properties: StateFlow<List<Property>?> =
        propertyRepository.getAllProperties()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tenants: StateFlow<List<Tenant>?> =
        tenantRepository.getAllTenants()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        propertyId: Long, tenantId: Long,
        startDate: Long, endDate: Long,
        monthlyRent: Double, deposit: Double, notes: String,
        onConflict: () -> Unit, onError: (Throwable) -> Unit, onDone: () -> Unit
    ) = viewModelScope.launch {
        try {
            val existing = contract.value?.contract
            val existingId = existing?.id ?: -1L
            val conflicts = contractRepository.countActiveForProperty(propertyId, existingId)
            if (conflicts > 0) {
                onConflict()
                return@launch
            }
            if (existing != null) {
                contractRepository.update(existing.copy(
                    propertyId = propertyId, tenantId = tenantId,
                    startDate = startDate, endDate = endDate,
                    monthlyRent = monthlyRent, depositAmount = deposit, notes = notes
                ))
            } else {
                contractRepository.insert(Contract(
                    propertyId = propertyId, tenantId = tenantId,
                    startDate = startDate, endDate = endDate,
                    monthlyRent = monthlyRent, depositAmount = deposit, notes = notes
                ))
            }
            onDone()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
