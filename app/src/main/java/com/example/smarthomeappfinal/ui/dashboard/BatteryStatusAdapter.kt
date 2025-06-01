package com.example.smarthomeappfinal.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthomeappfinal.databinding.ItemBatteryStatusBinding

class BatteryStatusAdapter : ListAdapter<BatteryStatus, BatteryStatusAdapter.BatteryStatusViewHolder>(BatteryStatusDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatteryStatusViewHolder {
        return BatteryStatusViewHolder(
            ItemBatteryStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BatteryStatusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BatteryStatusViewHolder(
        private val binding: ItemBatteryStatusBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: BatteryStatus) {
            binding.apply {
                tvDeviceName.text = item.deviceName
                progressBattery.progress = item.batteryPercent
                tvBatteryPercent.text = "${item.batteryPercent}%"
                tvTimeRemaining.text = item.timeRemaining
                  // Update battery level indicator color based on percentage
                val colorRes = when {
                    item.batteryPercent <= 20 -> com.google.android.material.R.attr.colorError
                    item.batteryPercent <= 40 -> android.R.attr.colorAccent // Using accent color for warning
                    else -> com.google.android.material.R.attr.colorPrimary
                }
                // Apply color to progress indicator
                progressBattery.setIndicatorColor(
                    com.google.android.material.color.MaterialColors.getColor(
                        progressBattery,
                        colorRes
                    )
                )
            }
        }
    }

    private class BatteryStatusDiffCallback : DiffUtil.ItemCallback<BatteryStatus>() {
        override fun areItemsTheSame(oldItem: BatteryStatus, newItem: BatteryStatus): Boolean {
            return oldItem.deviceName == newItem.deviceName
        }

        override fun areContentsTheSame(oldItem: BatteryStatus, newItem: BatteryStatus): Boolean {
            return oldItem == newItem
        }
    }
}
