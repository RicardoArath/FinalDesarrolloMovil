package com.example.rentflowmax.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.data.repository.MaintenanceRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MaintenanceDetailViewModel(
    private val maintenanceId: Long,
    private val repository: MaintenanceRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    val maintenance: StateFlow<Maintenance?> =
        repository.getMaintenanceById(maintenanceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val propertyName: StateFlow<String> = maintenance.flatMapLatest { m ->
        if (m != null) propertyRepository.getPropertyById(m.propertyId).map { it?.name ?: "" }
        else flowOf("")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun markAsComplete() = viewModelScope.launch {
        maintenance.value?.let { m ->
            repository.update(m.copy(
                status = Maintenance.STATUS_COMPLETED,
                resolvedDate = System.currentTimeMillis()
            ))
        }
    }

    fun delete(maintenance: Maintenance) = viewModelScope.launch {
        repository.delete(maintenance)
    }
}
