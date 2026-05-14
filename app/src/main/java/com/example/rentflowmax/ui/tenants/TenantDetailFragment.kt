package com.example.rentflowmax.ui.tenants

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
import com.example.rentflowmax.databinding.FragmentTenantDetailBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.ui.contracts.ContractAdapter
import kotlinx.coroutines.launch

class TenantDetailFragment : Fragment() {

    private var _binding: FragmentTenantDetailBinding? = null
    private val binding get() = _binding!!

    private val tenantId: Long by lazy { requireArguments().getLong("tenantId") }

    private val viewModel: TenantDetailViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            TenantDetailViewModel(tenantId, app.tenantRepository, app.contractRepository)
        }
    }

    private lateinit var contractsAdapter: ContractAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTenantDetailBinding.inflate(inflater, container, false)
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
                            R.id.action_tenantDetail_to_tenantForm,
                            Bundle().apply { putLong("tenantId", tenantId) }
                        )
                        true
                    }
                    R.id.action_delete -> { confirmDelete(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        contractsAdapter = ContractAdapter { contract ->
            findNavController().navigate(
                R.id.action_tenantDetail_to_contractDetail,
                Bundle().apply { putLong("contractId", contract.contract.id) }
            )
        }

        binding.rvContracts.apply {
            adapter = contractsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnCreateContract.setOnClickListener {
            findNavController().navigate(
                R.id.action_tenantDetail_to_contractForm,
                Bundle().apply {
                    putLong("contractId", -1L)
                    putLong("preselectedTenantId", tenantId)
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tenant.collect { data ->
                data ?: return@collect
                val tenant = data.tenant
                binding.tvTenantName.text = tenant.fullName
                binding.tvTenantId.text = tenant.idNumber
                binding.tvTenantPhone.text = tenant.phone
                binding.tvTenantEmail.text = tenant.email
                if (tenant.emergencyContactName.isNotBlank()) {
                    binding.tvEmergencyContact.text = "${tenant.emergencyContactName} - ${tenant.emergencyContactPhone}"
                    binding.tvEmergencyContact.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contracts.collect { contracts ->
                contractsAdapter.submitList(contracts)
                binding.tvNoContracts.visibility = if (contracts.isEmpty()) View.VISIBLE else View.GONE
                binding.rvContracts.visibility = if (contracts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun confirmDelete() {
        val tenant = viewModel.tenant.value?.tenant ?: return
        val contractCount = viewModel.contracts.value.size
        if (contractCount > 0) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_blocked_title)
                .setMessage(getString(R.string.delete_blocked_tenant_msg, contractCount))
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.tenant_delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.delete(tenant)
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
