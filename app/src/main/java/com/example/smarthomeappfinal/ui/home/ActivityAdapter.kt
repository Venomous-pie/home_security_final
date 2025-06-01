package com.example.smarthomeappfinal.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthomeappfinal.databinding.ItemActivityBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityAdapter : ListAdapter<ActivityItem, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(
            ItemActivityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(
        private val binding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ActivityItem) {            binding.apply {
                tvActivityTitle.text = item.title
                tvActivityDescription.text = "${item.device} • ${formatTime(item.timestamp)}"
            }
        }

        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
                else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(Date(timestamp))
            }
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityItem>() {
        override fun areItemsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem.timestamp == newItem.timestamp && 
                   oldItem.title == newItem.title &&
                   oldItem.device == newItem.device
        }

        override fun areContentsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem == newItem
        }
    }
}
