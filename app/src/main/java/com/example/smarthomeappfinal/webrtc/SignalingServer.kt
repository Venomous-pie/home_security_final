package com.example.smarthomeappfinal.webrtc

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.concurrent.TimeUnit

class SignalingServer(
    private val scope: CoroutineScope,
    private val userId: String,
    private val deviceId: String,
    private val securityToken: String,
    private val serverUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val gson = Gson()

    private val _connectionState = MutableStateFlow<SignalingState>(SignalingState.Disconnected)
    val connectionState: StateFlow<SignalingState> = _connectionState

    // Callbacks for WebRTC events
    var onOfferReceived: ((SessionDescription) -> Unit)? = null
    var onAnswerReceived: ((SessionDescription) -> Unit)? = null
    var onIceCandidateReceived: ((IceCandidate) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun connect() {
        val request = Request.Builder()
            .url("$serverUrl/ws?userId=$userId&deviceId=$deviceId")
            .addHeader("Authorization", "Bearer $securityToken")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                scope.launch(Dispatchers.Main) {
                    _connectionState.value = SignalingState.Connected
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleSignalingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scope.launch(Dispatchers.Main) {
                    _connectionState.value = SignalingState.Error(t.message ?: "Unknown error")
                    onError?.invoke(t.message ?: "Connection failed")
                }
                reconnectWithBackoff()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scope.launch(Dispatchers.Main) {
                    _connectionState.value = SignalingState.Disconnected
                }
            }
        })
    }

    fun sendOffer(sdp: SessionDescription, targetDeviceId: String) {
        val message = SignalingMessage(
            type = MessageType.OFFER,
            payload = SDPMessage(
                sdp = sdp.description,
                type = sdp.type.canonicalForm(),
                fromDeviceId = deviceId,
                toDeviceId = targetDeviceId
            )
        )
        sendMessage(message)
    }

    fun sendAnswer(sdp: SessionDescription, targetDeviceId: String) {
        val message = SignalingMessage(
            type = MessageType.ANSWER,
            payload = SDPMessage(
                sdp = sdp.description,
                type = sdp.type.canonicalForm(),
                fromDeviceId = deviceId,
                toDeviceId = targetDeviceId
            )
        )
        sendMessage(message)
    }

    fun sendIceCandidate(candidate: IceCandidate, targetDeviceId: String) {
        val message = SignalingMessage(
            type = MessageType.ICE_CANDIDATE,
            payload = IceCandidateMessage(
                sdpMid = candidate.sdpMid,
                sdpMLineIndex = candidate.sdpMLineIndex,
                candidate = candidate.sdp,
                fromDeviceId = deviceId,
                toDeviceId = targetDeviceId
            )
        )
        sendMessage(message)
    }

    private fun sendMessage(message: SignalingMessage) {
        val jsonMessage = gson.toJson(message)
        webSocket?.send(jsonMessage) ?: run {
            onError?.invoke("WebSocket not connected")
        }
    }

    private fun handleSignalingMessage(message: String) {
        try {
            val signalingMessage = gson.fromJson(message, SignalingMessage::class.java)
            when (signalingMessage.type) {
                MessageType.OFFER -> handleOffer(signalingMessage.payload as SDPMessage)
                MessageType.ANSWER -> handleAnswer(signalingMessage.payload as SDPMessage)
                MessageType.ICE_CANDIDATE -> handleIceCandidate(signalingMessage.payload as IceCandidateMessage)
                MessageType.ERROR -> handleError(signalingMessage.payload as ErrorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing signaling message: ${e.message}")
            onError?.invoke("Invalid message format")
        }
    }

    private fun handleOffer(message: SDPMessage) {
        val sessionDescription = SessionDescription(
            SessionDescription.Type.fromCanonicalForm(message.type),
            message.sdp
        )
        scope.launch(Dispatchers.Main) {
            onOfferReceived?.invoke(sessionDescription)
        }
    }

    private fun handleAnswer(message: SDPMessage) {
        val sessionDescription = SessionDescription(
            SessionDescription.Type.fromCanonicalForm(message.type),
            message.sdp
        )
        scope.launch(Dispatchers.Main) {
            onAnswerReceived?.invoke(sessionDescription)
        }
    }

    private fun handleIceCandidate(message: IceCandidateMessage) {
        val iceCandidate = IceCandidate(
            message.sdpMid,
            message.sdpMLineIndex,
            message.candidate
        )
        scope.launch(Dispatchers.Main) {
            onIceCandidateReceived?.invoke(iceCandidate)
        }
    }

    private fun handleError(error: ErrorMessage) {
        scope.launch(Dispatchers.Main) {
            onError?.invoke(error.message)
        }
    }

    private var reconnectAttempt = 0
    private fun reconnectWithBackoff() {
        scope.launch(Dispatchers.IO) {
            val delayMs = minOf(1000L * (1 shl reconnectAttempt), 30000L) // Max 30 seconds
            kotlinx.coroutines.delay(delayMs)
            reconnectAttempt++
            connect()
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _connectionState.value = SignalingState.Disconnected
    }

    companion object {
        private const val TAG = "SignalingServer"
    }
}

sealed class SignalingState {
    object Connected : SignalingState()
    object Disconnected : SignalingState()
    data class Error(val message: String) : SignalingState()
}

enum class MessageType {
    OFFER, ANSWER, ICE_CANDIDATE, ERROR
}

data class SignalingMessage(
    val type: MessageType,
    val payload: Any
)

data class SDPMessage(
    val sdp: String,
    val type: String,
    val fromDeviceId: String,
    val toDeviceId: String
)

data class IceCandidateMessage(
    val sdpMid: String?,
    val sdpMLineIndex: Int,
    val candidate: String,
    val fromDeviceId: String,
    val toDeviceId: String
)

data class ErrorMessage(
    val message: String
) 