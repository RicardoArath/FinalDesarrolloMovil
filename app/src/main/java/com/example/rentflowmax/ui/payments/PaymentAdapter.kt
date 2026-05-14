package com.example.rentflowmax.ui.payments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentflowmax.R
import com.example.rentflowmax.data.model.Payment
import com.example.rentflowmax.databinding.ItemPaymentBinding
import com.example.rentflowmax.util.CurrencyUtils.toCurrencyString
import com.example.rentflowmax.util.DateUtils.toDisplayDate

class PaymentAdapter(
    private val onItemClick: (Payment) -> Unit,
    private val contractLabelProvider: (Long) -> String
) : ListAdapter<Payment, PaymentAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Payment) {
            val ctx = binding.root.context
            binding.tvPropertyTenant.text = contractLabelProvider(item.contractId)
            binding.tvAmount.text = item.amount.toCurrencyString()
            binding.tvPaymentDate.text = item.paymentDate.toDisplayDate()
            binding.tvMethod.text = item.paymentMethod

            if (item.isLate) {
                binding.tvLateChip.visibility = View.VISIBLE
                binding.tvLateChip.text = ctx.getString(R.string.payment_late)
                binding.tvLateChip.setBackgroundColor(ctx.getColor(R.color.payment_late))
            } else {
                binding.tvLateChip.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(old: Payment, new: Payment) = old.id == new.id
        override fun areContentsTheSame(old: Payment, new: Payment) = old == new
    }
}
