package com.example.smarthomeappfinal.webrtc

import android.graphics.Bitmap
import android.os.Parcelable
import android.util.Base64
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.parcelize.Parcelize


@Parcelize
data class PairingData(
    val userId: String,
    val deviceId: String,
    val securityToken: String,
    val serverUrl: String,
    val roomId: String? = null
) : Parcelable {
    companion object {
        private val gson = Gson()

        fun fromQrContent(content: String): PairingData? {
            return try {
                val jsonString = String(Base64.decode(content, Base64.URL_SAFE))
                gson.fromJson(jsonString, PairingData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toQrContent(): String {
        val jsonString = gson.toJson(this)
        return Base64.encodeToString(jsonString.toByteArray(), Base64.URL_SAFE)
    }

    fun generateQrCode(width: Int = 600, height: Int = 600): Bitmap? {
        return try {
            val content = toQrContent()
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            null
        }
    }

    // Validate the security token (JWT)
    fun isTokenValid(): Boolean {
        return try {
            // Split the JWT token into its parts
            val parts = securityToken.split(".")
            if (parts.size != 3) return false

            // Decode the payload
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val payloadJson = gson.fromJson(payload, Map::class.java)

            // Check expiration
            val exp = (payloadJson["exp"] as? Double)?.toLong() ?: return false
            val currentTime = System.currentTimeMillis() / 1000
            
            // Token is valid if not expired
            exp > currentTime
        } catch (e: Exception) {
            false
        }
    }
} 