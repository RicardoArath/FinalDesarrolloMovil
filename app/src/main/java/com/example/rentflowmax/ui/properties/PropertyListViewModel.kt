package com.example.rentflowmax.ui.properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.relations.PropertyWithActiveContract
import com.example.rentflowmax.data.repository.PropertyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PropertyListViewModel(private val repository: PropertyRepository) : ViewModel() {

    val properties: StateFlow<List<PropertyWithActiveContract>> =
        repository.getAllPropertiesWithContracts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(property: Property) = viewModelScope.launch {
        repository.delete(property)
    }
}
