package com.example.rentflowmax.ui.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Contract
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.repository.ContractRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContractDetailViewModel(
    private val contractId: Long,
    private val repository: ContractRepository
) : ViewModel() {

    val contract: StateFlow<ContractWithDetails?> =
        repository.getContractWithDetailsById(contractId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(contract: Contract) = viewModelScope.launch {
        repository.delete(contract)
    }
}
