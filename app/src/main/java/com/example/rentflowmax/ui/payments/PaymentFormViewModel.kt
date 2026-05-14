package com.example.rentflowmax.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.Payment
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.PaymentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentFormViewModel(
    private val paymentId: Long,
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    val payment: StateFlow<Payment?> =
        if (paymentId != -1L) paymentRepository.getPaymentById(paymentId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        else MutableStateFlow(null)

    val activeContracts: StateFlow<List<ContractWithDetails>?> =
        contractRepository.getActiveContractsWithDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        contractId: Long, amount: Double, paymentDate: Long,
        periodMonth: Int, periodYear: Int, paymentMethod: String,
        receiptNumber: String, notes: String, isLate: Boolean,
        onDuplicate: () -> Unit, onError: (Throwable) -> Unit, onDone: () -> Unit
    ) = viewModelScope.launch {
        try {
            val existing = payment.value
            val existingId = existing?.id ?: -1L
            val duplicates = paymentRepository.countForContractPeriod(contractId, periodMonth, periodYear, existingId)
            if (duplicates > 0) {
                onDuplicate()
                return@launch
            }
            if (existing != null) {
                paymentRepository.update(existing.copy(
                    contractId = contractId, amount = amount, paymentDate = paymentDate,
                    periodMonth = periodMonth, periodYear = periodYear, paymentMethod = paymentMethod,
                    receiptNumber = receiptNumber, notes = notes, isLate = isLate
                ))
            } else {
                paymentRepository.insert(Payment(
                    contractId = contractId, amount = amount, paymentDate = paymentDate,
                    periodMonth = periodMonth, periodYear = periodYear, paymentMethod = paymentMethod,
                    receiptNumber = receiptNumber, notes = notes, isLate = isLate
                ))
            }
            onDone()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
