package com.example.rentflowmax.ui.properties

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
import com.example.rentflowmax.databinding.FragmentPropertyListBinding
import com.example.rentflowmax.ui.common.ViewModelFactory
import com.example.rentflowmax.util.observeSavedMessage
import kotlinx.coroutines.launch

class PropertyListFragment : Fragment() {

    private var _binding: FragmentPropertyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyListViewModel by viewModels {
        ViewModelFactory {
            val app = requireActivity().application as RentFlowMaxApp
            PropertyListViewModel(app.propertyRepository)
        }
    }

    private lateinit var adapter: PropertyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropertyAdapter { item ->
            findNavController().navigate(
                R.id.action_propertyList_to_propertyDetail,
                Bundle().apply { putLong("propertyId", item.property.id) }
            )
        }

        binding.recyclerView.apply {
            this.adapter = this@PropertyListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_propertyList_to_propertyForm)
        }

        observeSavedMessage(binding.root, binding.fabAdd)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.properties.collect { list ->
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
