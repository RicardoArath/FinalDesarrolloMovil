package com.example.rentflowmax.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.data.repository.ContractRepository
import com.example.rentflowmax.data.repository.MaintenanceRepository
import com.example.rentflowmax.data.repository.PaymentRepository
import com.example.rentflowmax.data.repository.PropertyRepository
import com.example.rentflowmax.util.DateUtils
import kotlinx.coroutines.flow.*
import java.util.Calendar

class DashboardViewModel(
    private val propertyRepository: PropertyRepository,
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    val rentedPropertyCount: StateFlow<Int> =
        contractRepository.getActiveContractCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPropertyCount: StateFlow<Int> =
        propertyRepository.getAllProperties()
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyIncome: StateFlow<Double> = run {
        val cal = Calendar.getInstance()
        paymentRepository.getTotalCollectedByPeriod(
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    val expiringContracts: StateFlow<List<ContractWithDetails>> =
        contractRepository.getActiveContractsWithDetails()
            .map { contracts ->
                contracts.filter { DateUtils.isExpiringSoon(it.contract.endDate) }
                    .sortedBy { it.contract.endDate }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMaintenanceCount: StateFlow<Int> =
        maintenanceRepository.getPendingMaintenanceCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
