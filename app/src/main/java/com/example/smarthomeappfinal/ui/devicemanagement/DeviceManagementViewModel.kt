package com.example.smarthomeappfinal.ui.devicemanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeappfinal.di.ServiceLocator
import com.example.smarthomeappfinal.network.DeviceResponse
import com.example.smarthomeappfinal.network.NetworkResult
import com.example.smarthomeappfinal.repository.DeviceRepository
import kotlinx.coroutines.launch

class DeviceManagementViewModel : ViewModel() {
    private val deviceRepository: DeviceRepository = ServiceLocator.provideDeviceRepository()

    private val _cameraDevices = MutableLiveData<List<DeviceResponse>>()
    val cameraDevices: LiveData<List<DeviceResponse>> = _cameraDevices

    private val _viewerDevices = MutableLiveData<List<DeviceResponse>>()
    val viewerDevices: LiveData<List<DeviceResponse>> = _viewerDevices

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            deviceRepository.getUserDevices().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val devices = result.data
                        _cameraDevices.value = devices.filter { it.type.lowercase() == "camera" }
                        _viewerDevices.value = devices.filter { it.type.lowercase() == "viewer" }
                    }
                    is NetworkResult.Error -> {
                        _error.value = result.message
                    }
                    is NetworkResult.Loading -> {
                        // Show loading state if needed
                    }
                }
            }
        }
    }    fun controlDevice(deviceId: String, command: String, parameters: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            deviceRepository.controlDevice(deviceId, command, parameters).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Refresh device list to show updated status
                        loadDevices()
                    }
                    is NetworkResult.Error -> {
                        _error.value = result.message
                    }
                    is NetworkResult.Loading -> {
                        // Show loading state if needed
                    }
                }
            }
        }
    }
}
