package com.example.rentflowmax.ui.contracts

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
import com.example.rentflowmax.databinding.FragmentContractDetailBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.ui.payments.PaymentAdapter
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils
import com.example.rentflowmax.util.DateUtils.toDisplayDate
import kotlinx.coroutines.launch

class ContractDetailFragment : Fragment() {

    private var _binding: FragmentContractDetailBinding? = null
    private val binding get() = _binding!!

    private val contractId: Long by lazy { requireArguments().getLong("contractId") }

    private val viewModel: ContractDetailViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            ContractDetailViewModel(contractId, app.contractRepository)
        }
    }

    private lateinit var paymentsAdapter: PaymentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContractDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_edit -> {
                        findNavController().navigate(
                            R.id.action_contractDetail_to_contractForm,
                            Bundle().apply { putLong("contractId", contractId) }
                        )
                        true
                    }
                    R.id.action_delete -> { confirmDelete(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        paymentsAdapter = PaymentAdapter(
            onItemClick = { payment ->
                findNavController().navigate(
                    R.id.action_contractDetail_to_paymentForm,
                    Bundle().apply { putLong("paymentId", payment.id) }
                )
            },
            contractLabelProvider = { id ->
                viewModel.contract.value?.let {
                    "${it.property.name} - ${it.tenant.fullName}"
                } ?: "Contrato #$id"
            }
        )

        binding.rvPayments.apply {
            adapter = paymentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnAddPayment.setOnClickListener {
            findNavController().navigate(
                R.id.action_contractDetail_to_paymentForm,
                Bundle().apply {
                    putLong("preselectedContractId", contractId)
                    putLong("paymentId", -1L)
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contract.collect { data ->
                data ?: return@collect
                val contract = data.contract

                binding.tvPropertyName.text = data.property.name
                binding.tvTenantName.text = data.tenant.fullName
                binding.tvContractPeriod.text = getString(
                    R.string.contract_period,
                    contract.startDate.toDisplayDate(),
                    contract.endDate.toDisplayDate()
                )
                binding.tvMonthlyRent.text = contract.monthlyRent.toCurrencyString()
                binding.tvDeposit.text = contract.depositAmount.toCurrencyString()

                if (contract.notes.isNotBlank()) {
                    binding.tvNotes.text = contract.notes
                    binding.tvNotes.visibility = View.VISIBLE
                }

                val daysLeft = DateUtils.daysUntil(contract.endDate)
                val isExpired = DateUtils.isExpired(contract.endDate)

                val (statusText, statusColor) = when {
                    !contract.isActive || isExpired -> Pair(getString(R.string.contract_expired), requireContext().getColor(R.color.status_expired))
                    DateUtils.isExpiringSoon(contract.endDate) -> Pair(getString(R.string.contract_days_remaining, daysLeft), requireContext().getColor(R.color.status_expiring))
                    else -> Pair(getString(R.string.contract_active), requireContext().getColor(R.color.status_rented))
                }
                binding.tvContractStatus.text = statusText
                binding.tvContractStatus.setBackgroundColor(statusColor)
                binding.tvDaysRemaining.text = if (!isExpired) getString(R.string.contract_days_remaining, daysLeft) else ""

                val totalPaid = data.payments.sumOf { it.amount }
                binding.tvTotalPaid.text = getString(R.string.contract_total_paid) + ": " + totalPaid.toCurrencyString()

                paymentsAdapter.submitList(data.payments)
            }
        }
    }

    private fun confirmDelete() {
        val data = viewModel.contract.value ?: return
        val contract = data.contract
        val paymentCount = data.payments.size
        val message = buildString {
            append(getString(R.string.contract_delete_confirm))
            if (paymentCount > 0) {
                append("\n\n")
                append(getString(R.string.contract_delete_warn_payments, paymentCount))
            }
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.delete(contract)
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
