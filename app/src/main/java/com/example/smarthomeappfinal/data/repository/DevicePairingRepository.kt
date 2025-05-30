package com.example.smarthomeappfinal.data.repository

import com.example.smarthomeappfinal.data.model.DevicePairing
import com.example.smarthomeappfinal.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DevicePairingRepository {
    private val database = FirebaseDatabase.getInstance()
    private val pairingsRef = database.getReference("device_pairings")

    suspend fun createPairing(devicePairing: DevicePairing): String {
        val pairCode = generatePairCode()
        val pairingWithCode = devicePairing.copy(pairCode = pairCode)
        pairingsRef.child(pairCode).setValue(pairingWithCode.toMap()).await()
        return pairCode
    }

    suspend fun getPairing(pairCode: String): DevicePairing? {
        val snapshot = pairingsRef.child(pairCode).get().await()
        return snapshot.getValue(DevicePairing::class.java)
    }

    fun observePairing(pairCode: String): Flow<DevicePairing?> = callbackFlow {
        val listener = pairingsRef.child(pairCode).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pairing = snapshot.getValue(DevicePairing::class.java)
                trySend(pairing)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose {
            pairingsRef.child(pairCode).removeEventListener(listener)
        }
    }

    suspend fun updatePairingMode(pairCode: String, mode: Int) {
        pairingsRef.child(pairCode).child("mode").setValue(mode).await()
    }

    suspend fun deactivatePairing(pairCode: String) {
        pairingsRef.child(pairCode).child("isActive").setValue(false).await()
    }

    private fun generatePairCode(): String {
        // Generate a 6-character alphanumeric code
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { allowedChars.random() }.joinToString("")
    }
} 