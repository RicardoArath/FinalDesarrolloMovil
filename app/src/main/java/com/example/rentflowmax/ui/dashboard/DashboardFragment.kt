package com.example.rentflowmax.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentflowmax.R
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.databinding.FragmentDashboardBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.ui.contracts.ContractAdapter
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            DashboardViewModel(
                app.propertyRepository,
                app.contractRepository,
                app.paymentRepository,
                app.maintenanceRepository
            )
        }
    }

    private lateinit var expiringContractsAdapter: ContractAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expiringContractsAdapter = ContractAdapter { contract ->
            findNavController().navigate(
                R.id.action_dashboard_to_contractDetail,
                Bundle().apply { putLong("contractId", contract.contract.id) }
            )
        }

        binding.rvExpiringContracts.apply {
            adapter = expiringContractsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.cardProperties.setOnClickListener {
            findNavController().navigate(R.id.propertyListFragment)
        }

        binding.cardIncome.setOnClickListener {
            findNavController().navigate(R.id.paymentListFragment)
        }

        binding.cardMaintenance.setOnClickListener {
            findNavController().navigate(R.id.maintenanceListFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            combine(viewModel.totalPropertyCount, viewModel.rentedPropertyCount) { total, rented ->
                total to rented
            }.collect { (total, rented) ->
                binding.tvRentedCount.text = rented.toString()
                binding.tvAvailableCount.text = (total - rented).coerceAtLeast(0).toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlyIncome.collect { income ->
                binding.tvMonthlyIncome.text = income.toCurrencyString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expiringContracts.collect { contracts ->
                expiringContractsAdapter.submitList(contracts)
                binding.tvNoExpiring.visibility = if (contracts.isEmpty()) View.VISIBLE else View.GONE
                binding.rvExpiringContracts.visibility = if (contracts.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingMaintenanceCount.collect { count ->
                binding.tvPendingMaintenanceCount.text = count.toString()
                binding.tvNoMaintenance.visibility = if (count == 0) View.VISIBLE else View.GONE
                binding.tvPendingMaintenanceCount.visibility = if (count == 0) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
