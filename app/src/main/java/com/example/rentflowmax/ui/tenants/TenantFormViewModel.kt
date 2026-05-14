package com.example.rentflowmax.ui.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.data.repository.TenantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TenantFormViewModel(
    private val tenantId: Long,
    private val repository: TenantRepository
) : ViewModel() {

    val tenant: StateFlow<Tenant?> =
        repository.getTenantById(tenantId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        firstName: String, lastName: String, email: String, phone: String,
        idNumber: String, emergencyContact: String, emergencyPhone: String,
        onError: (Throwable) -> Unit, onDone: () -> Unit
    ) = viewModelScope.launch {
        try {
            val existing = tenant.value
            if (existing != null) {
                repository.update(existing.copy(
                    firstName = firstName, lastName = lastName, email = email,
                    phone = phone, idNumber = idNumber,
                    emergencyContactName = emergencyContact, emergencyContactPhone = emergencyPhone
                ))
            } else {
                repository.insert(Tenant(
                    firstName = firstName, lastName = lastName, email = email,
                    phone = phone, idNumber = idNumber,
                    emergencyContactName = emergencyContact, emergencyContactPhone = emergencyPhone
                ))
            }
            onDone()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
