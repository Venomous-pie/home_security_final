package com.example.smarthomeappfinal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _recentActivities = MutableStateFlow<List<ActivityItem>>(emptyList())
    val recentActivities: StateFlow<List<ActivityItem>> = _recentActivities

    private val _cameraCount = MutableStateFlow(0)
    val cameraCount: StateFlow<Int> = _cameraCount

    private val _viewerCount = MutableStateFlow(0)
    val viewerCount: StateFlow<Int> = _viewerCount

    fun loadData() {
        viewModelScope.launch {
            // Simulate API call delay
            delay(500)
            
            // Load sample data
            _cameraCount.value = 3
            _viewerCount.value = 2
            
            _recentActivities.value = listOf(
                ActivityItem(
                    "Motion Detected",
                    "Front Door Camera",
                    System.currentTimeMillis() - 5 * 60 * 1000 // 5 minutes ago
                ),
                ActivityItem(
                    "Viewer Connected",
                    "Mobile App (iPhone)",
                    System.currentTimeMillis() - 15 * 60 * 1000 // 15 minutes ago
                ),
                ActivityItem(
                    "Camera Online",
                    "Backyard Camera",
                    System.currentTimeMillis() - 30 * 60 * 1000 // 30 minutes ago
                )
            )
        }
    }
}

data class ActivityItem(
    val title: String,
    val device: String,
    val timestamp: Long
)