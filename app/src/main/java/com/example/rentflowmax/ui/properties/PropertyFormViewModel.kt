package com.example.rentflowmax.ui.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PropertyFormViewModel(
    private val propertyId: Long,
    private val repository: PropertyRepository
) : ViewModel() {

    val property: StateFlow<Property?> =
        repository.getPropertyById(propertyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        name: String, address: String, type: String, monthlyRent: Double, description: String,
        onError: (Throwable) -> Unit, onDone: () -> Unit
    ) = viewModelScope.launch {
        try {
            val existing = property.value
            if (existing != null) {
                repository.update(existing.copy(name = name, address = address, type = type, monthlyRent = monthlyRent, description = description))
            } else {
                repository.insert(Property(name = name, address = address, type = type, monthlyRent = monthlyRent, description = description))
            }
            onDone()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
