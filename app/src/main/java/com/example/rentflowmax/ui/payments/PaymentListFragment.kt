package com.example.rentflowmax.ui.payments

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
import com.example.rentflowmax.databinding.FragmentPaymentListBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Locale

class PaymentListFragment : Fragment() {

    private var _binding: FragmentPaymentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentListViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            PaymentListViewModel(app.paymentRepository, app.contractRepository)
        }
    }

    private lateinit var adapter: PaymentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PaymentAdapter(
            onItemClick = { payment ->
                findNavController().navigate(
                    R.id.action_paymentList_to_paymentForm,
                    Bundle().apply { putLong("paymentId", payment.id) }
                )
            },
            contractLabelProvider = { id -> viewModel.contractLabels.value[id] ?: "Contrato #$id" }
        )

        binding.recyclerView.apply {
            this.adapter = this@PaymentListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_paymentList_to_paymentForm)
        }

        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.month.collect { updatePeriodLabel() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.year.collect { updatePeriodLabel() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.payments.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contractLabels.collect {
                adapter.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalCollected.collect { total ->
                binding.tvTotalCollected.text = getString(R.string.payment_total_period) + ": " + total.toCurrencyString()
            }
        }
    }

    private fun updatePeriodLabel() {
        val monthName = DateFormatSymbols(Locale.forLanguageTag("es-MX")).months[viewModel.month.value - 1]
            .replaceFirstChar { it.uppercase() }
        binding.tvPeriod.text = "$monthName ${viewModel.year.value}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
