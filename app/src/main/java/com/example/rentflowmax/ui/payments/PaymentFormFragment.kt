package com.example.rentflowmax.ui.payments

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
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.databinding.FragmentPaymentFormBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.DateUtils.toDisplayDate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class PaymentFormFragment : Fragment() {

    private var _binding: FragmentPaymentFormBinding? = null
    private val binding get() = _binding!!

    private val paymentId: Long by lazy { arguments?.getLong("paymentId", -1L) ?: -1L }
    private val preselectedContractId: Long by lazy { arguments?.getLong("preselectedContractId", -1L) ?: -1L }

    private val viewModel: PaymentFormViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            PaymentFormViewModel(paymentId, app.contractRepository, app.paymentRepository)
        }
    }

    private var selectedPaymentDate: Long = System.currentTimeMillis()
    private val contractList = mutableListOf<ContractWithDetails>()
    private var dependencyChecked = false
    private val months = DateFormatSymbols(Locale.forLanguageTag("es-MX")).months
        .filter { it.isNotEmpty() }
        .map { it.replaceFirstChar { c -> c.uppercase() } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState?.containsKey(KEY_PAYMENT_DATE) == true) {
            selectedPaymentDate = savedInstanceState.getLong(KEY_PAYMENT_DATE)
        }
        binding.etPaymentDate.setText(selectedPaymentDate.toDisplayDate())

        val methodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf(getString(R.string.payment_method_cash), getString(R.string.payment_method_transfer),
                getString(R.string.payment_method_check), getString(R.string.payment_method_card)))
        binding.acvMethod.setAdapter(methodAdapter)

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.acvMonth.setAdapter(monthAdapter)

        val cal = Calendar.getInstance()
        binding.acvMonth.setText(months[cal.get(Calendar.MONTH)], false)
        binding.etYear.setText(cal.get(Calendar.YEAR).toString())

        setupDatePicker()
        observeData()

        binding.acvContract.setOnItemClickListener { parent, _, position, _ ->
            val selectedLabel = parent.getItemAtPosition(position)?.toString() ?: return@setOnItemClickListener
            contractList.firstOrNull { "${it.property.name} - ${it.tenant.fullName}" == selectedLabel }?.let { selected ->
                binding.etAmount.setText(selected.contract.monthlyRent.toString())
            }
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun setupDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.payment_date))
            .setSelection(selectedPaymentDate)
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            selectedPaymentDate = millis
            binding.etPaymentDate.setText(millis.toDisplayDate())
        }

        binding.tilPaymentDate.setEndIconOnClickListener { picker.show(parentFragmentManager, "payDate") }
        binding.etPaymentDate.setOnClickListener { picker.show(parentFragmentManager, "payDate") }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeContracts.collect { contracts ->
                contracts ?: return@collect
                contractList.clear()
                contractList.addAll(contracts)
                val labels = contracts.map { "${it.property.name} - ${it.tenant.fullName}" }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
                binding.acvContract.setAdapter(adapter)

                if (preselectedContractId != -1L) {
                    val idx = contracts.indexOfFirst { it.contract.id == preselectedContractId }
                    if (idx >= 0) {
                        binding.acvContract.setText(labels[idx], false)
                        binding.etAmount.setText(contracts[idx].contract.monthlyRent.toString())
                    }
                }

                if (!dependencyChecked) {
                    dependencyChecked = true
                    if (contracts.isEmpty()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.dialog_no_contracts_title))
                            .setMessage(getString(R.string.dialog_no_contracts_msg))
                            .setPositiveButton(getString(R.string.dialog_btn_see_contracts)) { _, _ ->
                                findNavController().navigate(R.id.action_paymentForm_to_contractList)
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
    }

    private fun save() {
        val contractLabel = binding.acvContract.text?.toString() ?: ""
        val amountStr = binding.etAmount.text?.toString()?.trim() ?: ""
        val method = binding.acvMethod.text?.toString() ?: ""
        val monthName = binding.acvMonth.text?.toString() ?: ""
        val yearStr = binding.etYear.text?.toString()?.trim() ?: ""
        val receipt = binding.etReceipt.text?.toString()?.trim() ?: ""
        val notes = binding.etNotes.text?.toString()?.trim() ?: ""
        val isLate = binding.cbIsLate.isChecked

        val selectedContract = contractList.firstOrNull { "${it.property.name} - ${it.tenant.fullName}" == contractLabel }
        val amount = amountStr.toDoubleOrNull()
        val year = yearStr.toIntOrNull()
        val monthIdx = months.indexOf(monthName)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        var valid = true
        if (selectedContract == null) { binding.tilContract.error = getString(R.string.required_field); valid = false } else binding.tilContract.error = null
        when {
            amount == null -> { binding.tilAmount.error = getString(R.string.invalid_amount); valid = false }
            amount <= 0.0 -> { binding.tilAmount.error = getString(R.string.amount_must_be_positive); valid = false }
            else -> binding.tilAmount.error = null
        }
        if (method.isEmpty()) { binding.tilMethod.error = getString(R.string.required_field); valid = false } else binding.tilMethod.error = null
        when {
            year == null -> { binding.tilYear.error = getString(R.string.required_field); valid = false }
            year !in 2000..(currentYear + 1) -> { binding.tilYear.error = getString(R.string.year_out_of_range, currentYear + 1); valid = false }
            else -> binding.tilYear.error = null
        }
        if (monthIdx < 0) { binding.tilMonth.error = getString(R.string.required_field); valid = false } else binding.tilMonth.error = null
        if (selectedPaymentDate > System.currentTimeMillis()) {
            binding.tilPaymentDate.error = getString(R.string.future_date_not_allowed)
            valid = false
        } else binding.tilPaymentDate.error = null

        if (!valid) return

        binding.btnSave.isEnabled = false
        viewModel.save(
            selectedContract!!.contract.id, amount!!, selectedPaymentDate,
            monthIdx + 1, year!!, method, receipt, notes, isLate,
            onDuplicate = {
                binding.tilMonth.error = getString(R.string.payment_duplicate_period)
                binding.btnSave.isEnabled = true
            },
            onError = {
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, R.string.msg_save_error, Snackbar.LENGTH_LONG).show()
            },
            onDone = {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "saved_message", getString(R.string.msg_payment_saved)
                )
                findNavController().navigateUp()
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_PAYMENT_DATE, selectedPaymentDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_PAYMENT_DATE = "paymentDate"
    }
}
