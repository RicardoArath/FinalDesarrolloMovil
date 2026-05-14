package com.example.rentflowmax.ui.tenants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rentflowmax.R
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.databinding.FragmentTenantFormBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TenantFormFragment : Fragment() {

    private var _binding: FragmentTenantFormBinding? = null
    private val binding get() = _binding!!

    private val tenantId: Long by lazy { arguments?.getLong("tenantId", -1L) ?: -1L }

    private val viewModel: TenantFormViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            TenantFormViewModel(tenantId, app.tenantRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tenantId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.tenant.collect { tenant ->
                    tenant ?: return@collect
                    binding.etFirstName.setText(tenant.firstName)
                    binding.etLastName.setText(tenant.lastName)
                    binding.etPhone.setText(tenant.phone)
                    binding.etEmail.setText(tenant.email)
                    binding.etIdNumber.setText(tenant.idNumber)
                    binding.etEmergencyContact.setText(tenant.emergencyContactName)
                    binding.etEmergencyPhone.setText(tenant.emergencyContactPhone)
                }
            }
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val firstName = binding.etFirstName.text?.toString()?.trim() ?: ""
        val lastName = binding.etLastName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val idNumber = binding.etIdNumber.text?.toString()?.trim() ?: ""
        val emergencyContact = binding.etEmergencyContact.text?.toString()?.trim() ?: ""
        val emergencyPhone = binding.etEmergencyPhone.text?.toString()?.trim() ?: ""

        var valid = true
        if (firstName.isEmpty()) { binding.tilFirstName.error = getString(R.string.required_field); valid = false } else binding.tilFirstName.error = null
        if (lastName.isEmpty()) { binding.tilLastName.error = getString(R.string.required_field); valid = false } else binding.tilLastName.error = null
        if (phone.isEmpty()) { binding.tilPhone.error = getString(R.string.required_field); valid = false } else binding.tilPhone.error = null
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email); valid = false
        } else binding.tilEmail.error = null

        if (!valid) return

        binding.btnSave.isEnabled = false
        viewModel.save(
            firstName, lastName, email, phone, idNumber, emergencyContact, emergencyPhone,
            onError = {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, R.string.msg_save_error, Snackbar.LENGTH_LONG).show()
            },
            onDone = {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "saved_message", getString(R.string.msg_tenant_saved)
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
