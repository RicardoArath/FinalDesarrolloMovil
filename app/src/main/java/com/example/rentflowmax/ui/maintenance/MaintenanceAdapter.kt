package com.example.rentflowmax.ui.maintenance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentflowmax.R
import com.example.rentflowmax.data.model.Maintenance
import com.example.rentflowmax.databinding.ItemMaintenanceBinding
import com.example.rentflowmax.util.DateUtils.toDisplayDate

class MaintenanceAdapter(
    private val onItemClick: (Maintenance) -> Unit,
    private val propertyNameProvider: (Long) -> String
) : ListAdapter<Maintenance, MaintenanceAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMaintenanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Maintenance) {
            val ctx = binding.root.context
            binding.tvTitle.text = item.title
            binding.tvProperty.text = propertyNameProvider(item.propertyId)
            binding.tvReportedDate.text = item.reportedDate.toDisplayDate()

            binding.tvPriority.text = item.priority
            binding.tvPriority.setBackgroundColor(
                when (item.priority) {
                    Maintenance.PRIORITY_URGENT -> ctx.getColor(R.color.priority_urgent)
                    Maintenance.PRIORITY_HIGH -> ctx.getColor(R.color.priority_high)
                    Maintenance.PRIORITY_MEDIUM -> ctx.getColor(R.color.priority_medium)
                    else -> ctx.getColor(R.color.priority_low)
                }
            )

            binding.tvStatus.text = item.status
            binding.tvStatus.setBackgroundColor(
                when (item.status) {
                    Maintenance.STATUS_PENDING -> ctx.getColor(R.color.maintenance_pending)
                    Maintenance.STATUS_IN_PROGRESS -> ctx.getColor(R.color.maintenance_in_progress)
                    else -> ctx.getColor(R.color.maintenance_completed)
                }
            )

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaintenanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Maintenance>() {
        override fun areItemsTheSame(old: Maintenance, new: Maintenance) = old.id == new.id
        override fun areContentsTheSame(old: Maintenance, new: Maintenance) = old == new
    }
}
