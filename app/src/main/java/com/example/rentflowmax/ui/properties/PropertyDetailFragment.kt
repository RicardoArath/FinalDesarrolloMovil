package com.example.rentflowmax.ui.properties

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentflowmax.R
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.databinding.FragmentPropertyDetailBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.ui.contracts.ContractAdapter
import com.example.rentflowmax.ui.maintenance.MaintenanceAdapter
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils
import com.example.rentflowmax.util.DateUtils.toDisplayDate
import kotlinx.coroutines.launch

class PropertyDetailFragment : Fragment() {

    private var _binding: FragmentPropertyDetailBinding? = null
    private val binding get() = _binding!!

    private val propertyId: Long by lazy { requireArguments().getLong("propertyId") }

    private val viewModel: PropertyDetailViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            PropertyDetailViewModel(propertyId, app.propertyRepository, app.contractRepository, app.maintenanceRepository)
        }
    }

    private lateinit var contractsAdapter: ContractAdapter
    private lateinit var maintenanceAdapter: MaintenanceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPropertyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupAdapters()
        observeData()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_edit -> {
                        findNavController().navigate(
                            R.id.action_propertyDetail_to_propertyForm,
                            Bundle().apply { putLong("propertyId", propertyId) }
                        )
                        true
                    }
                    R.id.action_delete -> { confirmDelete(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupAdapters() {
        contractsAdapter = ContractAdapter { contract ->
            findNavController().navigate(
                R.id.action_propertyDetail_to_contractDetail,
                Bundle().apply { putLong("contractId", contract.contract.id) }
            )
        }

        maintenanceAdapter = MaintenanceAdapter(
            onItemClick = { maintenance ->
                findNavController().navigate(
                    R.id.action_propertyDetail_to_maintenanceDetail,
                    Bundle().apply { putLong("maintenanceId", maintenance.id) }
                )
            },
            propertyNameProvider = { viewModel.propertyWithContracts.value?.property?.name ?: "" }
        )

        binding.rvContracts.apply {
            adapter = contractsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.rvMaintenance.apply {
            adapter = maintenanceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.propertyWithContracts.collect { data ->
                data ?: return@collect
                val property = data.property
                maintenanceAdapter.notifyDataSetChanged()

                binding.tvPropertyName.text = property.name
                binding.tvPropertyType.text = property.type
                binding.tvPropertyAddress.text = property.address
                binding.tvMonthlyRent.text = property.monthlyRent.toCurrencyString()

                if (property.description.isNotBlank()) {
                    binding.tvDescription.text = property.description
                    binding.tvDescription.visibility = View.VISIBLE
                }

                val activeContract = data.activeContract
                if (activeContract != null) {
                    binding.layoutActiveContract.visibility = View.VISIBLE
                    binding.layoutNoContract.visibility = View.GONE

                    binding.tvContractPeriod.text = requireContext().getString(
                        R.string.contract_period,
                        activeContract.startDate.toDisplayDate(),
                        activeContract.endDate.toDisplayDate()
                    )

                    val daysLeft = DateUtils.daysUntil(activeContract.endDate)
                    val isExpired = DateUtils.isExpired(activeContract.endDate)

                    val (chipText, chipColor) = when {
                        isExpired -> Pair(getString(R.string.contract_expired), requireContext().getColor(R.color.status_expired))
                        DateUtils.isExpiringSoon(activeContract.endDate) -> Pair(getString(R.string.contract_days_remaining, daysLeft), requireContext().getColor(R.color.status_expiring))
                        else -> Pair(getString(R.string.contract_days_remaining, daysLeft), requireContext().getColor(R.color.status_rented))
                    }
                    binding.tvContractDaysRemaining.text = chipText
                    binding.tvContractDaysRemaining.setBackgroundColor(chipColor)
                } else {
                    binding.layoutActiveContract.visibility = View.GONE
                    binding.layoutNoContract.visibility = View.VISIBLE
                }

                binding.btnCreateContract.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_propertyDetail_to_contractForm,
                        Bundle().apply {
                            putLong("contractId", -1L)
                            putLong("preselectedPropertyId", propertyId)
                        }
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contractHistory.collect { contracts ->
                contractsAdapter.submitList(contracts)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.maintenance.collect { list ->
                maintenanceAdapter.submitList(list)
            }
        }
    }

    private fun confirmDelete() {
        val property = viewModel.propertyWithContracts.value?.property ?: return
        val contractCount = viewModel.contractHistory.value.size
        if (contractCount > 0) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_blocked_title)
                .setMessage(getString(R.string.delete_blocked_property_msg, contractCount))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }
        val maintenanceCount = viewModel.maintenance.value.size
        val message = if (maintenanceCount > 0) {
            getString(R.string.property_delete_confirm) + "\n\n" +
                    getString(R.string.property_delete_warn_maintenance, maintenanceCount)
        } else {
            getString(R.string.property_delete_confirm)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.delete(property)
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
