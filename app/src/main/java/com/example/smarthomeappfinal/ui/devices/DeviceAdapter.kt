package com.example.smarthomeappfinal.ui.devices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthomeappfinal.databinding.ItemDeviceBinding
import com.example.smarthomeappfinal.network.DeviceResponse

class DeviceAdapter(
    private val onDeviceAction: (DeviceResponse, String) -> Unit
) : ListAdapter<DeviceResponse, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onDeviceAction)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceAction: (DeviceResponse, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: DeviceResponse) {
            with(binding) {
                textViewDeviceName.text = device.name
                textViewDeviceType.text = device.type
                textViewDeviceStatus.text = device.status
                textViewLastUpdated.text = device.lastUpdated

                // Configure action buttons based on device type
                when (device.type.lowercase()) {
                    "light" -> setupLightControls(device)
                    "thermostat" -> setupThermostatControls(device)
                    "camera" -> setupCameraControls(device)
                    else -> setupDefaultControls(device)
                }
            }
        }

        private fun setupLightControls(device: DeviceResponse) {
            binding.buttonPrimary.apply {
                text = if (device.status == "ON") "Turn Off" else "Turn On"
                setOnClickListener {
                    onDeviceAction(device, if (device.status == "ON") "turn_off" else "turn_on")
                }
            }
        }

        private fun setupThermostatControls(device: DeviceResponse) {
            binding.buttonPrimary.apply {
                text = "Adjust Temperature"
                setOnClickListener {
                    onDeviceAction(device, "show_temperature_dialog")
                }
            }
        }

        private fun setupCameraControls(device: DeviceResponse) {
            binding.buttonPrimary.apply {
                text = "View Stream"
                setOnClickListener {
                    onDeviceAction(device, "view_stream")
                }
            }
        }

        private fun setupDefaultControls(device: DeviceResponse) {
            binding.buttonPrimary.apply {
                text = "Details"
                setOnClickListener {
                    onDeviceAction(device, "show_details")
                }
            }
        }
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<DeviceResponse>() {
        override fun areItemsTheSame(oldItem: DeviceResponse, newItem: DeviceResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DeviceResponse, newItem: DeviceResponse): Boolean {
            return oldItem == newItem
        }
    }
} 