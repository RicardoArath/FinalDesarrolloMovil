package com.example.rentflowmax.ui.contracts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentflowmax.R
import com.example.rentflowmax.data.model.relations.ContractWithDetails
import com.example.rentflowmax.databinding.ItemContractBinding
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils
import com.example.rentflowmax.util.DateUtils.toDisplayDate

class ContractAdapter(
    private val onItemClick: (ContractWithDetails) -> Unit
) : ListAdapter<ContractWithDetails, ContractAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemContractBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContractWithDetails) {
            val ctx = binding.root.context
            binding.tvPropertyName.text = item.property.name
            binding.tvTenantName.text = item.tenant.fullName
            binding.tvContractPeriod.text = ctx.getString(
                R.string.contract_period,
                item.contract.startDate.toDisplayDate(),
                item.contract.endDate.toDisplayDate()
            )
            binding.tvMonthlyRent.text = item.contract.monthlyRent.toCurrencyString()

            val daysLeft = DateUtils.daysUntil(item.contract.endDate)
            val isExpired = DateUtils.isExpired(item.contract.endDate)

            when {
                !item.contract.isActive || isExpired -> {
                    binding.tvStatusChip.text = ctx.getString(R.string.contract_expired)
                    binding.tvStatusChip.setBackgroundColor(ctx.getColor(R.color.status_expired))
                    binding.tvDaysRemaining.text = ctx.getString(R.string.contract_expired)
                }
                DateUtils.isExpiringSoon(item.contract.endDate) -> {
                    binding.tvStatusChip.text = ctx.getString(R.string.contract_active)
                    binding.tvStatusChip.setBackgroundColor(ctx.getColor(R.color.status_expiring))
                    binding.tvDaysRemaining.text = ctx.getString(R.string.contract_days_remaining, daysLeft)
                }
                else -> {
                    binding.tvStatusChip.text = ctx.getString(R.string.contract_active)
                    binding.tvStatusChip.setBackgroundColor(ctx.getColor(R.color.status_rented))
                    binding.tvDaysRemaining.text = ctx.getString(R.string.contract_days_remaining, daysLeft)
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ContractWithDetails>() {
        override fun areItemsTheSame(old: ContractWithDetails, new: ContractWithDetails) =
            old.contract.id == new.contract.id

        override fun areContentsTheSame(old: ContractWithDetails, new: ContractWithDetails) =
            old == new
    }
}
