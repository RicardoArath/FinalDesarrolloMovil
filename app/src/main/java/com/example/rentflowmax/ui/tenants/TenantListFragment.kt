package com.example.rentflowmax.ui.tenants

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
import com.example.rentflowmax.databinding.FragmentTenantListBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.observeSavedMessage
import kotlinx.coroutines.launch

class TenantListFragment : Fragment() {

    private var _binding: FragmentTenantListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantListViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            TenantListViewModel(app.tenantRepository)
        }
    }

    private lateinit var adapter: TenantAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TenantAdapter { item ->
            findNavController().navigate(
                R.id.action_tenantList_to_tenantDetail,
                Bundle().apply { putLong("tenantId", item.tenant.id) }
            )
        }

        binding.recyclerView.apply {
            this.adapter = this@TenantListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_tenantList_to_tenantForm)
        }

        observeSavedMessage(binding.root, binding.fabAdd)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tenants.collect { list ->
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
