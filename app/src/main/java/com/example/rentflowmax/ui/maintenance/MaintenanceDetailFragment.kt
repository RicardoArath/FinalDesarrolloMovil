package com.example.rentflowmax.ui.maintenance

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rentflowmax.R
import com.example.rentflowmax.RentFlowMaxApp
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.databinding.FragmentMaintenanceDetailBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils.toDisplayDate
import kotlinx.coroutines.launch

class MaintenanceDetailFragment : Fragment() {

    private var _binding: FragmentMaintenanceDetailBinding? = null
    private val binding get() = _binding!!

    private val maintenanceId: Long by lazy { requireArguments().getLong("maintenanceId") }

    private val viewModel: MaintenanceDetailViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            MaintenanceDetailViewModel(maintenanceId, app.maintenanceRepository, app.propertyRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaintenanceDetailBinding.inflate(inflater, container, false)
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
                            R.id.action_maintenanceDetail_to_maintenanceForm,
                            Bundle().apply { putLong("maintenanceId", maintenanceId) }
                        )
                        true
                    }
                    R.id.action_delete -> { confirmDelete(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.btnMarkComplete.setOnClickListener {
            viewModel.markAsComplete()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.propertyName.collect { name ->
                binding.tvProperty.text = name
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.maintenance.collect { m ->
                m ?: return@collect
                binding.tvTitle.text = m.title
                binding.tvDescription.text = m.description
                binding.tvReportedDate.text = m.reportedDate.toDisplayDate()
                binding.tvEstimatedCost.text = m.estimatedCost.toCurrencyString()
                binding.tvActualCost.text = m.actualCost.toCurrencyString()

                binding.tvResolvedDate.text = m.resolvedDate?.toDisplayDate() ?: "-"

                if (m.technicianName.isNotBlank()) {
                    binding.tvTechnician.text = getString(R.string.maintenance_technician) + ": " + m.technicianName
                    binding.tvTechnician.visibility = View.VISIBLE
                }

                if (m.notes.isNotBlank()) {
                    binding.tvNotes.text = m.notes
                    binding.tvNotes.visibility = View.VISIBLE
                }

                binding.tvPriority.text = m.priority
                binding.tvPriority.setBackgroundColor(
                    when (m.priority) {
                        Maintenance.PRIORITY_URGENT -> requireContext().getColor(R.color.priority_urgent)
                        Maintenance.PRIORITY_HIGH -> requireContext().getColor(R.color.priority_high)
                        Maintenance.PRIORITY_MEDIUM -> requireContext().getColor(R.color.priority_medium)
                        else -> requireContext().getColor(R.color.priority_low)
                    }
                )

                binding.tvStatus.text = m.status
                binding.tvStatus.setBackgroundColor(
                    when (m.status) {
                        Maintenance.STATUS_PENDING -> requireContext().getColor(R.color.maintenance_pending)
                        Maintenance.STATUS_IN_PROGRESS -> requireContext().getColor(R.color.maintenance_in_progress)
                        else -> requireContext().getColor(R.color.maintenance_completed)
                    }
                )

                binding.btnMarkComplete.visibility =
                    if (m.status != Maintenance.STATUS_COMPLETED) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmDelete() {
        val m = viewModel.maintenance.value ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.maintenance_delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.delete(m)
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
