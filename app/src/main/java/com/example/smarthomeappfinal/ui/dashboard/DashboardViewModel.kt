package com.example.smarthomeappfinal.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _systemHealth = MutableStateFlow(SystemHealth(0, 0, 0))
    val systemHealth: StateFlow<SystemHealth> = _systemHealth

    private val _networkStats = MutableStateFlow(NetworkStats(0, 0))
    val networkStats: StateFlow<NetworkStats> = _networkStats

    private val _deviceStats = MutableStateFlow<List<DeviceStats>>(emptyList())
    val deviceStats: StateFlow<List<DeviceStats>> = _deviceStats

    private val _batteryStatus = MutableStateFlow<List<BatteryStatus>>(emptyList())
    val batteryStatus: StateFlow<List<BatteryStatus>> = _batteryStatus

    fun loadData() {
        viewModelScope.launch {
            // Simulate API call delay
            delay(500)            // Sample system health data
            _systemHealth.value = SystemHealth(
                storageUsed = (6.5 * 1024 * 1024 * 1024).toLong(), // 6.5 GB
                storageTotal = 10L * 1024 * 1024 * 1024, // 10 GB
                storageUsagePercent = 65
            )

            // Sample network statistics
            _networkStats.value = NetworkStats(
                uploadSpeed = (2.5 * 1024 * 1024).toLong(), // 2.5 Mbps
                downloadSpeed = (5.8 * 1024 * 1024).toLong()  // 5.8 Mbps
            )

            // Sample device statistics
            _deviceStats.value = listOf(
                DeviceStats(
                    "Camera Activity",
                    "High motion detection in last hour",
                    85
                ),
                DeviceStats(
                    "System Performance",
                    "Normal operation",
                    95
                )
            )

            // Sample battery status
            _batteryStatus.value = listOf(
                BatteryStatus(
                    "Front Door Camera",
                    85,
                    "Charging"
                ),
                BatteryStatus(
                    "Back Door Camera",
                    72,
                    "Discharging"
                ),
                BatteryStatus(
                    "Garage Camera",
                    95,
                    "Charging"
                )
            )
        }
    }
}

data class SystemHealth(
    val storageUsed: Long,
    val storageTotal: Long,
    val storageUsagePercent: Int
)

data class NetworkStats(
    val uploadSpeed: Long,
    val downloadSpeed: Long
)

data class DeviceStats(
    val title: String,
    val description: String,
    val healthPercent: Int
)

data class BatteryStatus(
    val deviceName: String,
    val batteryPercent: Int,
    val timeRemaining: String
)