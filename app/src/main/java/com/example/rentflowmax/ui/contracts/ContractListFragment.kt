package com.example.rentflowmax.ui.contracts

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
import com.example.rentflowmax.databinding.FragmentContractListBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.observeSavedMessage
import kotlinx.coroutines.launch

class ContractListFragment : Fragment() {

    private var _binding: FragmentContractListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContractListViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            ContractListViewModel(app.contractRepository)
        }
    }

    private lateinit var adapter: ContractAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContractListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContractAdapter { item ->
            findNavController().navigate(
                R.id.action_contractList_to_contractDetail,
                Bundle().apply { putLong("contractId", item.contract.id) }
            )
        }

        binding.recyclerView.apply {
            this.adapter = this@ContractListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_contractList_to_contractForm)
        }

        observeSavedMessage(binding.root, binding.fabAdd)

        binding.chipAll.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(ContractFilter.ALL) }
        binding.chipActive.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(ContractFilter.ACTIVE) }
        binding.chipExpired.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(ContractFilter.EXPIRED) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contracts.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
