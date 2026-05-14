package com.example.rentflowmax.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Payment
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.PaymentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PaymentListViewModel(
    private val repository: PaymentRepository,
    contractRepository: ContractRepository
) : ViewModel() {

    val contractLabels: StateFlow<Map<Long, String>> =
        contractRepository.getAllContractsWithDetails()
            .map { contracts ->
                contracts.associate { it.contract.id to "${it.property.name} - ${it.tenant.fullName}" }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _month = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val month: StateFlow<Int> = _month
    val year: StateFlow<Int> = _year

    val payments: StateFlow<List<Payment>> =
        combine(_month, _year) { m, y -> Pair(m, y) }
            .flatMapLatest { (m, y) -> repository.getPaymentsByPeriod(m, y) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollected: StateFlow<Double> =
        combine(_month, _year) { m, y -> Pair(m, y) }
            .flatMapLatest { (m, y) -> repository.getTotalCollectedByPeriod(m, y) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun delete(payment: Payment) = viewModelScope.launch {
        repository.delete(payment)
    }

    fun previousMonth() {
        if (_month.value == 1) {
            _month.value = 12
            _year.value--
        } else {
            _month.value--
        }
    }

    fun nextMonth() {
        if (_month.value == 12) {
            _month.value = 1
            _year.value++
        } else {
            _month.value++
        }
    }
}
