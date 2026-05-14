package com.example.rentflowmax.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.data.repository.MaintenanceRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.*

enum class MaintenanceFilter { ALL, PENDING, IN_PROGRESS, COMPLETED }

class MaintenanceListViewModel(
    private val repository: MaintenanceRepository,
    propertyRepository: PropertyRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(MaintenanceFilter.ALL)

    private val allMaintenance: StateFlow<List<Maintenance>> =
        repository.getAllMaintenance()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenance: StateFlow<List<Maintenance>> =
        combine(allMaintenance, _filter) { list, filter ->
            when (filter) {
                MaintenanceFilter.ALL -> list
                MaintenanceFilter.PENDING -> list.filter { it.status == Maintenance.STATUS_PENDING }
                MaintenanceFilter.IN_PROGRESS -> list.filter { it.status == Maintenance.STATUS_IN_PROGRESS }
                MaintenanceFilter.COMPLETED -> list.filter { it.status == Maintenance.STATUS_COMPLETED }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val propertyNames: StateFlow<Map<Long, String>> =
        propertyRepository.getAllProperties()
            .map { props -> props.associate { it.id to it.name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setFilter(filter: MaintenanceFilter) { _filter.value = filter }
}
