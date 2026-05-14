package com.example.rentflowmax.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.repository.MaintenanceRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MaintenanceFormViewModel(
    private val maintenanceId: Long,
    private val maintenanceRepository: MaintenanceRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    val maintenance: StateFlow<Maintenance?> =
        if (maintenanceId != -1L) maintenanceRepository.getMaintenanceById(maintenanceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        else MutableStateFlow(null)

    val properties: StateFlow<List<Property>?> =
        propertyRepository.getAllProperties()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        propertyId: Long, title: String, description: String, priority: String,
        status: String, estimatedCost: Double, actualCost: Double,
        technicianName: String, notes: String,
        onError: (Throwable) -> Unit, onDone: () -> Unit
    ) = viewModelScope.launch {
        try {
            val existing = maintenance.value
            if (existing != null) {
                maintenanceRepository.update(existing.copy(
                    propertyId = propertyId, title = title, description = description,
                    priority = priority, status = status,
                    estimatedCost = estimatedCost, actualCost = actualCost,
                    technicianName = technicianName, notes = notes,
                    resolvedDate = if (status == Maintenance.STATUS_COMPLETED && existing.resolvedDate == null)
                        System.currentTimeMillis() else existing.resolvedDate
                ))
            } else {
                maintenanceRepository.insert(Maintenance(
                    propertyId = propertyId, title = title, description = description,
                    priority = priority, status = status,
                    estimatedCost = estimatedCost, actualCost = actualCost,
                    technicianName = technicianName, notes = notes
                ))
            }
            onDone()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
