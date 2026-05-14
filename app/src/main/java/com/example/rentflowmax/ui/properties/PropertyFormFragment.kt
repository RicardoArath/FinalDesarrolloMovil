package com.example.rentflowmax.ui.properties

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
import com.example.rentflowmax.databinding.FragmentPropertyFormBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PropertyFormFragment : Fragment() {

    private var _binding: FragmentPropertyFormBinding? = null
    private val binding get() = _binding!!

    private val propertyId: Long by lazy { arguments?.getLong("propertyId", -1L) ?: -1L }

    private val viewModel: PropertyFormViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            PropertyFormViewModel(propertyId, app.propertyRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPropertyFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = listOf(
            getString(R.string.type_apartment),
            getString(R.string.type_house),
            getString(R.string.type_commercial),
            getString(R.string.type_warehouse),
            getString(R.string.type_office)
        )
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.acvType.setAdapter(typeAdapter)

        if (propertyId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.property.collect { property ->
                    property ?: return@collect
                    binding.etName.setText(property.name)
                    binding.etAddress.setText(property.address)
                    binding.acvType.setText(property.type, false)
                    binding.etMonthlyRent.setText(property.monthlyRent.toString())
                    binding.etDescription.setText(property.description)
                }
            }
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val type = binding.acvType.text?.toString()?.trim() ?: ""
        val rentStr = binding.etMonthlyRent.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""

        var valid = true
        if (name.isEmpty()) { binding.tilName.error = getString(R.string.required_field); valid = false } else binding.tilName.error = null
        if (address.isEmpty()) { binding.tilAddress.error = getString(R.string.required_field); valid = false } else binding.tilAddress.error = null
        if (type.isEmpty()) { binding.tilType.error = getString(R.string.required_field); valid = false } else binding.tilType.error = null
        val rent = rentStr.toDoubleOrNull()
        if (rent == null) { binding.tilMonthlyRent.error = getString(R.string.invalid_amount); valid = false } else binding.tilMonthlyRent.error = null

        if (!valid) return

        binding.btnSave.isEnabled = false
        viewModel.save(
            name, address, type, rent!!, description,
            onError = {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, R.string.msg_save_error, Snackbar.LENGTH_LONG).show()
            },
            onDone = {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "saved_message", getString(R.string.msg_property_saved)
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
