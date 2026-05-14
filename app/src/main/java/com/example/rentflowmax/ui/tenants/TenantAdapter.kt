package com.example.rentflowmax.ui.tenants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentflowmax.R
import com.example.rentflowmax.data.model.relations.TenantWithContracts
import com.example.rentflowmax.databinding.ItemTenantBinding

class TenantAdapter(
    private val onItemClick: (TenantWithContracts) -> Unit
) : ListAdapter<TenantWithContracts, TenantAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemTenantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TenantWithContracts) {
            binding.tvTenantName.text = item.tenant.fullName
            binding.tvTenantPhone.text = item.tenant.phone
            binding.tvTenantId.text = item.tenant.idNumber

            if (item.hasActiveContract) {
                binding.tvActiveBadge.visibility = View.VISIBLE
                binding.tvActiveBadge.setBackgroundColor(
                    binding.root.context.getColor(R.color.status_rented)
                )
            } else {
                binding.tvActiveBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TenantWithContracts>() {
        override fun areItemsTheSame(old: TenantWithContracts, new: TenantWithContracts) =
            old.tenant.id == new.tenant.id

        override fun areContentsTheSame(old: TenantWithContracts, new: TenantWithContracts) =
            old == new
    }
}
