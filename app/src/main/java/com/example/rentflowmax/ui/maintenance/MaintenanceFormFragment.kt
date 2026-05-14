package com.example.rentflowmax.ui.maintenance

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.data.model.Property
import com.example.rentflowmax.databinding.FragmentMaintenanceFormBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

class MaintenanceFormFragment : Fragment() {

    private var _binding: FragmentMaintenanceFormBinding? = null
    private val binding get() = _binding!!

    private val maintenanceId: Long by lazy { arguments?.getLong("maintenanceId", -1L) ?: -1L }
    private val preselectedPropertyId: Long by lazy { arguments?.getLong("preselectedPropertyId", -1L) ?: -1L }

    private val viewModel: MaintenanceFormViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            MaintenanceFormViewModel(maintenanceId, app.maintenanceRepository, app.propertyRepository)
        }
    }

    private val propertyList = mutableListOf<Property>()
    private var dependencyChecked = false
    private var pendingPropertyIdToSelect: Long = -1L
    private val priorities = listOf(
        Maintenance.PRIORITY_LOW, Maintenance.PRIORITY_MEDIUM,
        Maintenance.PRIORITY_HIGH, Maintenance.PRIORITY_URGENT
    )
    private val statuses = listOf(
        Maintenance.STATUS_PENDING, Maintenance.STATUS_IN_PROGRESS, Maintenance.STATUS_COMPLETED
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaintenanceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities)
        binding.acvPriority.setAdapter(priorityAdapter)
        binding.acvPriority.setText(Maintenance.PRIORITY_MEDIUM, false)

        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.acvStatus.setAdapter(statusAdapter)
        binding.acvStatus.setText(Maintenance.STATUS_PENDING, false)

        observeData()
        binding.btnSave.setOnClickListener { save() }
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

                val idToSelect = when {
                    pendingPropertyIdToSelect != -1L -> pendingPropertyIdToSelect
                    preselectedPropertyId != -1L -> preselectedPropertyId
                    else -> -1L
                }
                if (idToSelect != -1L) {
                    val match = props.firstOrNull { it.id == idToSelect }
                    if (match != null) {
                        binding.acvProperty.setText(match.name, false)
                        pendingPropertyIdToSelect = -1L
                    }
                }

                if (!dependencyChecked) {
                    dependencyChecked = true
                    if (props.isEmpty()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.dialog_no_properties_title))
                            .setMessage(getString(R.string.dialog_no_properties_msg))
                            .setPositiveButton(getString(R.string.dialog_btn_register_property)) { _, _ ->
                                findNavController().navigate(R.id.action_maintenanceForm_to_propertyForm)
                            }
                            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                findNavController().navigateUp()
                            }
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }

        if (maintenanceId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.maintenance.collect { m ->
                    m ?: return@collect
                    binding.etTitle.setText(m.title)
                    binding.etDescription.setText(m.description)
                    binding.acvPriority.setText(m.priority, false)
                    binding.acvStatus.setText(m.status, false)
                    binding.etEstimatedCost.setText(m.estimatedCost.toString())
                    binding.etActualCost.setText(m.actualCost.toString())
                    binding.etTechnician.setText(m.technicianName)
                    binding.etNotes.setText(m.notes)

                    val prop = propertyList.firstOrNull { it.id == m.propertyId }
                    if (prop != null) {
                        binding.acvProperty.setText(prop.name, false)
                    } else {
                        pendingPropertyIdToSelect = m.propertyId
                    }
                }
            }
        }
    }

    private fun save() {
        val propertyName = binding.acvProperty.text?.toString() ?: ""
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val priority = binding.acvPriority.text?.toString() ?: ""
        val status = binding.acvStatus.text?.toString() ?: ""
        val estimatedCostStr = binding.etEstimatedCost.text?.toString()?.trim() ?: ""
        val actualCostStr = binding.etActualCost.text?.toString()?.trim() ?: ""
        val estimatedCost = estimatedCostStr.toDoubleOrNull() ?: 0.0
        val actualCost = actualCostStr.toDoubleOrNull() ?: 0.0
        val technician = binding.etTechnician.text?.toString()?.trim() ?: ""
        val notes = binding.etNotes.text?.toString()?.trim() ?: ""

        val selectedProperty = propertyList.firstOrNull { it.name == propertyName }

        var valid = true
        if (selectedProperty == null) { binding.tilProperty.error = getString(R.string.required_field); valid = false } else binding.tilProperty.error = null
        if (title.isEmpty()) { binding.tilTitle.error = getString(R.string.required_field); valid = false } else binding.tilTitle.error = null
        if (description.isEmpty()) { binding.tilDescription.error = getString(R.string.required_field); valid = false } else binding.tilDescription.error = null
        if (estimatedCostStr.isNotEmpty() && (estimatedCostStr.toDoubleOrNull() == null || estimatedCostStr.toDouble() < 0.0)) {
            binding.tilEstimatedCost.error = getString(R.string.amount_must_be_nonnegative); valid = false
        } else binding.tilEstimatedCost.error = null
        if (actualCostStr.isNotEmpty() && (actualCostStr.toDoubleOrNull() == null || actualCostStr.toDouble() < 0.0)) {
            binding.tilActualCost.error = getString(R.string.amount_must_be_nonnegative); valid = false
        } else binding.tilActualCost.error = null

        if (!valid) return

        binding.btnSave.isEnabled = false
        viewModel.save(
            selectedProperty!!.id, title, description, priority, status,
            estimatedCost, actualCost, technician, notes,
            onError = {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, R.string.msg_save_error, Snackbar.LENGTH_LONG).show()
            },
            onDone = {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "saved_message", getString(R.string.msg_maintenance_saved)
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
