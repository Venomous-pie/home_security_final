package com.example.smarthomeappfinal.network

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Firestore data model
data class Device(
    @DocumentId val id: String = "", // Firestore document ID
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val status: String = "",
    val lastUpdated: Timestamp = Timestamp.now(),
    val temperature: Float? = null,
    val humidity: Float? = null,
    val batteryLevel: Int? = null
)

class FirestoreService {
    private val db: FirebaseFirestore = Firebase.firestore
    private val devicesCollection = "devices"

    // Get real-time updates for user's devices
    fun getUserDevicesFlow(userId: String): Flow<List<Device>> = callbackFlow {
        val subscription = db.collection(devicesCollection)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val devices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Device>()
                } ?: emptyList()
                
                trySend(devices)
            }

        awaitClose { subscription.remove() }
    }

    // Pair a new device
    suspend fun pairDevice(userId: String, request: PairDeviceRequest): PairDeviceResponse {
        return try {
            val deviceData = hashMapOf(
                "userId" to userId,
                "name" to request.deviceName,
                "type" to request.deviceType,
                "status" to "offline",
                "lastUpdated" to Timestamp.now()
            )

            val docRef = db.collection(devicesCollection).add(deviceData).await()

            PairDeviceResponse(
                success = true,
                message = "Device paired successfully",
                deviceId = docRef.id
            )
        } catch (e: Exception) {
            PairDeviceResponse(
                success = false,
                message = "Failed to pair device: ${e.message}",
                deviceId = null
            )
        }
    }

    // Get device status with real-time updates
    fun getDeviceStatusFlow(deviceId: String): Flow<Device?> = callbackFlow {
        val subscription = db.collection(devicesCollection)
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val device = snapshot?.toObject<Device>()
                trySend(device)
            }

        awaitClose { subscription.remove() }
    }

    // Control device
    suspend fun controlDevice(deviceId: String, request: DeviceControlRequest): DeviceControlResponse {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to request.command,
                "lastUpdated" to Timestamp.now()
            )
            
            // Add any additional parameters from the request
            updates.putAll(request.parameters)

            db.collection(devicesCollection)
                .document(deviceId)
                .update(updates)
                .await()

            DeviceControlResponse(
                success = true,
                message = "Device control command sent successfully",
                newStatus = request.command
            )
        } catch (e: Exception) {
            DeviceControlResponse(
                success = false,
                message = "Failed to control device: ${e.message}",
                newStatus = null
            )
        }
    }
} 