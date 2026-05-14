package com.example.rentflowmax.ui.contracts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rentflowmax.R
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.data.model.Tenant
import com.example.rentflowmax.databinding.FragmentContractFormBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.DateUtils.toDisplayDate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ContractFormFragment : Fragment() {

    private var _binding: FragmentContractFormBinding? = null
    private val binding get() = _binding!!

    private val contractId: Long by lazy { arguments?.getLong("contractId", -1L) ?: -1L }
    private val preselectedPropertyId: Long by lazy { arguments?.getLong("preselectedPropertyId", -1L) ?: -1L }
    private val preselectedTenantId: Long by lazy { arguments?.getLong("preselectedTenantId", -1L) ?: -1L }

    private val viewModel: ContractFormViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            ContractFormViewModel(contractId, app.contractRepository, app.propertyRepository, app.tenantRepository)
        }
    }

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null
    private val propertyList = mutableListOf<Property>()
    private val tenantList = mutableListOf<Tenant>()
    private var propertiesLoaded = false
    private var tenantsLoaded = false
    private var dependencyChecked = false
    private var pendingPropertyIdToSelect: Long = -1L
    private var pendingTenantIdToSelect: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContractFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            if (it.containsKey(KEY_START_DATE)) selectedStartDate = it.getLong(KEY_START_DATE)
            if (it.containsKey(KEY_END_DATE)) selectedEndDate = it.getLong(KEY_END_DATE)
            selectedStartDate?.let { d -> binding.etStartDate.setText(d.toDisplayDate()) }
            selectedEndDate?.let { d -> binding.etEndDate.setText(d.toDisplayDate()) }
        }

        setupDatePickers()
        observeData()
        binding.btnSave.setOnClickListener { save() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedStartDate?.let { outState.putLong(KEY_START_DATE, it) }
        selectedEndDate?.let { outState.putLong(KEY_END_DATE, it) }
    }

    companion object {
        private const val KEY_START_DATE = "startDate"
        private const val KEY_END_DATE = "endDate"
    }

    private fun setupDatePickers() {
        val startPicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.contract_start_date))
            .build()

        val endPicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.contract_end_date))
            .build()

        startPicker.addOnPositiveButtonClickListener { millis ->
            selectedStartDate = millis
            binding.etStartDate.setText(millis.toDisplayDate())
        }

        endPicker.addOnPositiveButtonClickListener { millis ->
            selectedEndDate = millis
            binding.etEndDate.setText(millis.toDisplayDate())
        }

        binding.tilStartDate.setEndIconOnClickListener { startPicker.show(parentFragmentManager, "startDate") }
        binding.etStartDate.setOnClickListener { startPicker.show(parentFragmentManager, "startDate") }
        binding.tilEndDate.setEndIconOnClickListener { endPicker.show(parentFragmentManager, "endDate") }
        binding.etEndDate.setOnClickListener { endPicker.show(parentFragmentManager, "endDate") }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.properties.collect { props ->
                props ?: return@collect
                propertyList.clear()
                propertyList.addAll(props)
                val names = props.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                binding.acvProperty.setAdapter(adapter)

                val propIdToSelect = when {
                    pendingPropertyIdToSelect != -1L -> pendingPropertyIdToSelect
                    preselectedPropertyId != -1L -> preselectedPropertyId
                    else -> -1L
                }
                if (propIdToSelect != -1L) {
                    val match = props.firstOrNull { it.id == propIdToSelect }
                    if (match != null) {
                        binding.acvProperty.setText(match.name, false)
                        pendingPropertyIdToSelect = -1L
                    }
                }

                if (!propertiesLoaded) {
                    propertiesLoaded = true
                    checkDependencies()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tenants.collect { tenants ->
                tenants ?: return@collect
                tenantList.clear()
                tenantList.addAll(tenants)
                val names = tenants.map { it.fullName }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                binding.acvTenant.setAdapter(adapter)

                val tenantIdToSelect = when {
                    pendingTenantIdToSelect != -1L -> pendingTenantIdToSelect
                    preselectedTenantId != -1L -> preselectedTenantId
                    else -> -1L
                }
                if (tenantIdToSelect != -1L) {
                    val match = tenants.firstOrNull { it.id == tenantIdToSelect }
                    if (match != null) {
                        binding.acvTenant.setText(match.fullName, false)
                        pendingTenantIdToSelect = -1L
                    }
                }

                if (!tenantsLoaded) {
                    tenantsLoaded = true
                    checkDependencies()
                }
            }
        }

        if (contractId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.contract.collect { data ->
                    data ?: return@collect
                    val contract = data.contract
                    selectedStartDate = contract.startDate
                    selectedEndDate = contract.endDate
                    binding.etStartDate.setText(contract.startDate.toDisplayDate())
                    binding.etEndDate.setText(contract.endDate.toDisplayDate())
                    binding.etMonthlyRent.setText(contract.monthlyRent.toString())
                    binding.etDeposit.setText(contract.depositAmount.toString())
                    binding.etNotes.setText(contract.notes)
                    if (propertyList.any { it.id == data.property.id }) {
                        binding.acvProperty.setText(data.property.name, false)
                    } else {
                        pendingPropertyIdToSelect = data.property.id
                    }
                    if (tenantList.any { it.id == data.tenant.id }) {
                        binding.acvTenant.setText(data.tenant.fullName, false)
                    } else {
                        pendingTenantIdToSelect = data.tenant.id
                    }
                }
            }
        }
    }

    private fun checkDependencies() {
        if (!propertiesLoaded || !tenantsLoaded || dependencyChecked) return
        dependencyChecked = true
        when {
            propertyList.isEmpty() -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_no_properties_title))
                .setMessage(getString(R.string.dialog_no_properties_msg))
                .setPositiveButton(getString(R.string.dialog_btn_register_property)) { _, _ ->
                    findNavController().navigate(R.id.action_contractForm_to_propertyForm)
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    findNavController().navigateUp()
                }
                .setCancelable(false)
                .show()
            tenantList.isEmpty() -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_no_tenants_title))
                .setMessage(getString(R.string.dialog_no_tenants_msg))
                .setPositiveButton(getString(R.string.dialog_btn_register_tenant)) { _, _ ->
                    findNavController().navigate(R.id.action_contractForm_to_tenantForm)
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    findNavController().navigateUp()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun save() {
        val propertyName = binding.acvProperty.text?.toString() ?: ""
        val tenantName = binding.acvTenant.text?.toString() ?: ""
        val rentStr = binding.etMonthlyRent.text?.toString()?.trim() ?: ""
        val depositStr = binding.etDeposit.text?.toString()?.trim() ?: ""
        val notes = binding.etNotes.text?.toString()?.trim() ?: ""

        val selectedProperty = propertyList.firstOrNull { it.name == propertyName }
        val selectedTenant = tenantList.firstOrNull { it.fullName == tenantName }

        var valid = true
        if (selectedProperty == null) { binding.tilProperty.error = getString(R.string.required_field); valid = false } else binding.tilProperty.error = null
        if (selectedTenant == null) { binding.tilTenant.error = getString(R.string.required_field); valid = false } else binding.tilTenant.error = null
        if (selectedStartDate == null) { binding.tilStartDate.error = getString(R.string.required_field); valid = false } else binding.tilStartDate.error = null
        if (selectedEndDate == null) { binding.tilEndDate.error = getString(R.string.required_field); valid = false } else binding.tilEndDate.error = null
        if (selectedStartDate != null && selectedEndDate != null && selectedEndDate!! <= selectedStartDate!!) {
            binding.tilEndDate.error = getString(R.string.invalid_end_date)
            valid = false
        }
        val rent = rentStr.toDoubleOrNull()
        when {
            rent == null -> { binding.tilMonthlyRent.error = getString(R.string.invalid_amount); valid = false }
            rent <= 0.0 -> { binding.tilMonthlyRent.error = getString(R.string.amount_must_be_positive); valid = false }
            else -> binding.tilMonthlyRent.error = null
        }
        val depositParsed = depositStr.toDoubleOrNull()
        if (depositStr.isNotEmpty() && (depositParsed == null || depositParsed < 0.0)) {
            binding.tilDeposit.error = getString(R.string.amount_must_be_nonnegative)
            valid = false
        } else binding.tilDeposit.error = null

        if (!valid) return

        val deposit = depositParsed ?: 0.0

        binding.btnSave.isEnabled = false
        viewModel.save(
            selectedProperty!!.id, selectedTenant!!.id, selectedStartDate!!, selectedEndDate!!,
            rent!!, deposit, notes,
            onConflict = {
                binding.tilProperty.error = getString(R.string.property_already_rented)
                binding.btnSave.isEnabled = true
            },
            onError = {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, R.string.msg_save_error, Snackbar.LENGTH_LONG).show()
            },
            onDone = {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "saved_message", getString(R.string.msg_contract_saved)
                )
                findNavController().navigateUp()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
