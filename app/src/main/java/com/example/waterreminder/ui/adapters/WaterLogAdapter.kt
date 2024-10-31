package com.example.waterreminder.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.waterreminder.data.WaterLog
import com.example.waterreminder.databinding.ItemWaterLogBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WaterLogAdapter(
    private val onDeleteClick: (WaterLog) -> Unit
) : ListAdapter<WaterLog, WaterLogAdapter.WaterLogViewHolder>(WaterLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterLogViewHolder {
        val binding = ItemWaterLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WaterLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaterLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WaterLogViewHolder(
        private val binding: ItemWaterLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(waterLog: WaterLog) {
            binding.apply {
                amountText.text = "${waterLog.amount}ml"
                timeText.text = timeFormat.format(waterLog.timestamp)
                deleteButton.setOnClickListener { onDeleteClick(waterLog) }
            }
        }
    }
}

class WaterLogDiffCallback : DiffUtil.ItemCallback<WaterLog>() {
    override fun areItemsTheSame(oldItem: WaterLog, newItem: WaterLog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WaterLog, newItem: WaterLog): Boolean {
        return oldItem == newItem
    }
}