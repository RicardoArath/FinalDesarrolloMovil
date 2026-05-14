package com.example.rentflowmax.ui.contracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ContractFilter { ALL, ACTIVE, EXPIRED }

class ContractListViewModel(private val repository: ContractRepository) : ViewModel() {

    private val _filter = MutableStateFlow(ContractFilter.ALL)
    val filter: StateFlow<ContractFilter> = _filter

    private val allContracts: StateFlow<List<ContractWithDetails>> =
        repository.getAllContractsWithDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contracts: StateFlow<List<ContractWithDetails>> =
        combine(allContracts, _filter) { contracts, filter ->
            when (filter) {
                ContractFilter.ALL -> contracts
                ContractFilter.ACTIVE -> contracts.filter { it.contract.isActive && !DateUtils.isExpired(it.contract.endDate) }
                ContractFilter.EXPIRED -> contracts.filter { !it.contract.isActive || DateUtils.isExpired(it.contract.endDate) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: ContractFilter) { _filter.value = filter }
}
