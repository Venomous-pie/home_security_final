package com.example.smarthomeappfinal.repository

import com.example.smarthomeappfinal.auth.AuthManager
import com.example.smarthomeappfinal.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeviceRepository : BaseRepository() {
    private val apiService = NetworkModule.provideApiService()
    private val authManager = AuthManager.getInstance()

    fun getUserDevices(): Flow<NetworkResult<List<DeviceResponse>>> = flow {
        emit(NetworkResult.Loading)
        val token = authManager.currentUser?.getIdToken(false)?.result?.token
            ?: throw IllegalStateException("User not authenticated")
        
        emit(safeApiCall { 
            apiService.getUserDevices("Bearer $token")
        })
    }

    fun pairDevice(deviceId: String, type: String, name: String): Flow<NetworkResult<PairDeviceResponse>> = flow {
        emit(NetworkResult.Loading)
        val token = authManager.currentUser?.getIdToken(false)?.result?.token
            ?: throw IllegalStateException("User not authenticated")

        val request = PairDeviceRequest(
            deviceId = deviceId,
            deviceType = type,
            deviceName = name
        )
        
        emit(safeApiCall {
            apiService.pairDevice("Bearer $token", request)
        })
    }

    fun getDeviceStatus(deviceId: String): Flow<NetworkResult<DeviceStatusResponse>> = flow {
        emit(NetworkResult.Loading)
        val token = authManager.currentUser?.getIdToken(false)?.result?.token
            ?: throw IllegalStateException("User not authenticated")
        
        emit(safeApiCall {
            apiService.getDeviceStatus("Bearer $token", deviceId)
        })
    }

    fun controlDevice(
        deviceId: String,
        command: String,
        parameters: Map<String, Any>
    ): Flow<NetworkResult<DeviceControlResponse>> = flow {
        emit(NetworkResult.Loading)
        val token = authManager.currentUser?.getIdToken(false)?.result?.token
            ?: throw IllegalStateException("User not authenticated")

        val request = DeviceControlRequest(
            command = command,
            parameters = parameters
        )
        
        emit(safeApiCall {
            apiService.controlDevice("Bearer $token", deviceId, request)
        })
    }

    companion object {
        @Volatile
        private var instance: DeviceRepository? = null

        fun getInstance(): DeviceRepository {
            return instance ?: synchronized(this) {
                instance ?: DeviceRepository().also { instance = it }
            }
        }
    }
} 