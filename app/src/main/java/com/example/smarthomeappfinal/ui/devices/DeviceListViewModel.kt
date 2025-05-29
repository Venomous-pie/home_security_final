package com.example.smarthomeappfinal.ui.devices

import com.example.smarthomeappfinal.base.BaseViewModel
import com.example.smarthomeappfinal.di.ServiceLocator
import com.example.smarthomeappfinal.network.DeviceResponse
import com.example.smarthomeappfinal.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceListViewModel : BaseViewModel() {
    private val deviceRepository = ServiceLocator.provideDeviceRepository()

    private val _devices = MutableStateFlow<List<DeviceResponse>>(emptyList())
    val devices: StateFlow<List<DeviceResponse>> = _devices.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        launchIO {
            deviceRepository.getUserDevices()
                .collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _devices.value = result.data
                            _refreshing.value = false
                        }
                        is NetworkResult.Error -> {
                            emitError(result.message)
                            _refreshing.value = false
                        }
                        is NetworkResult.Loading -> {
                            _refreshing.value = true
                        }
                    }
                }
        }
    }

    fun controlDevice(deviceId: String, command: String, parameters: Map<String, Any> = emptyMap()) {
        launchIO {
            deviceRepository.controlDevice(deviceId, command, parameters)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            // Refresh device list to show updated status
                            loadDevices()
                        }
                        is NetworkResult.Error -> {
                            emitError(result.message)
                        }
                        is NetworkResult.Loading -> {
                            setLoading(true)
                        }
                    }
                }
        }
    }
} 