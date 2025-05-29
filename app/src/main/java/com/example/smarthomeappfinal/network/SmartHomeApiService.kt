package com.example.smarthomeappfinal.network

import retrofit2.http.*

interface SmartHomeApiService {
    companion object {
        const val BASE_URL = "https://api.smarthome.com/" // Replace with your actual API base URL
    }

    @GET("devices")
    suspend fun getUserDevices(
        @Header("Authorization") token: String
    ): List<DeviceResponse>

    @POST("devices/pair")
    suspend fun pairDevice(
        @Header("Authorization") token: String,
        @Body request: PairDeviceRequest
    ): PairDeviceResponse

    @GET("devices/{deviceId}/status")
    suspend fun getDeviceStatus(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String
    ): DeviceStatusResponse

    @POST("devices/{deviceId}/control")
    suspend fun controlDevice(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String,
        @Body request: DeviceControlRequest
    ): DeviceControlResponse
}

// Data classes for requests and responses
data class DeviceResponse(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val lastUpdated: String
)

data class PairDeviceRequest(
    val deviceId: String,
    val deviceType: String,
    val deviceName: String
)

data class PairDeviceResponse(
    val success: Boolean,
    val message: String,
    val deviceId: String?
)

data class DeviceStatusResponse(
    val deviceId: String,
    val status: String,
    val temperature: Float?,
    val humidity: Float?,
    val batteryLevel: Int?,
    val lastUpdated: String
)

data class DeviceControlRequest(
    val command: String,
    val parameters: Map<String, Any>
)

data class DeviceControlResponse(
    val success: Boolean,
    val message: String,
    val newStatus: String?
) 