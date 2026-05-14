package com.example.rentflowmax.ui.maintenance

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
import com.example.rentflowmax.databinding.FragmentMaintenanceListBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

class MaintenanceListFragment : Fragment() {

    private var _binding: FragmentMaintenanceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceListViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            MaintenanceListViewModel(app.maintenanceRepository, app.propertyRepository)
        }
    }

    private lateinit var adapter: MaintenanceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaintenanceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MaintenanceAdapter(
            onItemClick = { maintenance ->
                findNavController().navigate(
                    R.id.action_maintenanceList_to_maintenanceDetail,
                    Bundle().apply { putLong("maintenanceId", maintenance.id) }
                )
            },
            propertyNameProvider = { id -> viewModel.propertyNames.value[id] ?: "" }
        )

        binding.recyclerView.apply {
            this.adapter = this@MaintenanceListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_maintenanceList_to_maintenanceForm)
        }

        binding.chipAll.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(MaintenanceFilter.ALL) }
        binding.chipPending.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(MaintenanceFilter.PENDING) }
        binding.chipInProgress.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(MaintenanceFilter.IN_PROGRESS) }
        binding.chipCompleted.setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setFilter(MaintenanceFilter.COMPLETED) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.maintenance.collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.propertyNames.collect {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
