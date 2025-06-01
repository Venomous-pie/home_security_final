package com.example.smarthomeappfinal.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthomeappfinal.databinding.ItemDeviceStatBinding

class DeviceStatAdapter : ListAdapter<DeviceStats, DeviceStatAdapter.DeviceStatViewHolder>(DeviceStatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceStatViewHolder {
        return DeviceStatViewHolder(
            ItemDeviceStatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DeviceStatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceStatViewHolder(
        private val binding: ItemDeviceStatBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: DeviceStats) {
            binding.apply {
                tvStatTitle.text = item.title
                tvStatDescription.text = item.description
                progressStat.progress = item.healthPercent
                tvStatPercentage.text = "${item.healthPercent}%"
            }
        }
    }

    private class DeviceStatDiffCallback : DiffUtil.ItemCallback<DeviceStats>() {
        override fun areItemsTheSame(oldItem: DeviceStats, newItem: DeviceStats): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DeviceStats, newItem: DeviceStats): Boolean {
            return oldItem == newItem
        }
    }
}
