package com.example.rentflowmax.ui.properties

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentflowmax.R
import com.example.rentflowmax.data.model.relations.PropertyWithActiveContract
import com.example.rentflowmax.databinding.ItemPropertyBinding
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils
import com.example.rentflowmax.util.DateUtils.toDisplayDate

class PropertyAdapter(
    private val onItemClick: (PropertyWithActiveContract) -> Unit
) : ListAdapter<PropertyWithActiveContract, PropertyAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PropertyWithActiveContract) {
            binding.tvPropertyName.text = item.property.name
            binding.tvPropertyType.text = item.property.type
            binding.tvPropertyAddress.text = item.property.address
            binding.tvMonthlyRent.text = item.property.monthlyRent.toCurrencyString()

            val activeContract = item.activeContract
            val ctx = binding.root.context
            if (activeContract != null) {
                binding.tvStatusChip.text = ctx.getString(
                    R.string.property_status_rented, activeContract.endDate.toDisplayDate()
                )
                binding.tvStatusChip.setBackgroundColor(ctx.getColor(R.color.status_rented))
                val daysLeft = DateUtils.daysUntil(activeContract.endDate)
                binding.tvTenantInfo.text = ctx.getString(R.string.contract_days_remaining, daysLeft)
                binding.tvTenantInfo.visibility = android.view.View.VISIBLE
            } else {
                binding.tvStatusChip.text = ctx.getString(R.string.property_status_available)
                binding.tvStatusChip.setBackgroundColor(ctx.getColor(R.color.status_available))
                binding.tvTenantInfo.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PropertyWithActiveContract>() {
        override fun areItemsTheSame(old: PropertyWithActiveContract, new: PropertyWithActiveContract) =
            old.property.id == new.property.id

        override fun areContentsTheSame(old: PropertyWithActiveContract, new: PropertyWithActiveContract) =
            old == new
    }
}
