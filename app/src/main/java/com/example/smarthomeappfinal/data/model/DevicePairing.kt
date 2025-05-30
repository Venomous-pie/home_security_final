package com.example.smarthomeappfinal.data.model

data class DevicePairing(
    val deviceId: String = "",
    val userId: String = "",
    val mode: Int = 0,  // 0 for MONITOR, 1 for CAMERA
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val pairCode: String = "",
    val deviceName: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "deviceId" to deviceId,
            "userId" to userId,
            "mode" to mode,
            "timestamp" to timestamp,
            "isActive" to isActive,
            "pairCode" to pairCode,
            "deviceName" to deviceName
        )
    }
} 