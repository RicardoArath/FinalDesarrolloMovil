package com.example.rentflowmax.ui.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.model.relations.PropertyWithActiveContract
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.MaintenanceRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PropertyDetailViewModel(
    private val propertyId: Long,
    private val propertyRepository: PropertyRepository,
    private val contractRepository: ContractRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    val propertyWithContracts: StateFlow<PropertyWithActiveContract?> =
        propertyRepository.getPropertyWithContractsById(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contractHistory: StateFlow<List<ContractWithDetails>> =
        contractRepository.getContractsForProperty(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenance: StateFlow<List<Maintenance>> =
        maintenanceRepository.getMaintenanceForProperty(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(property: Property) = viewModelScope.launch {
        propertyRepository.delete(property)
    }
}
