package com.example.smarthomeappfinal.webrtc

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.*
import java.util.concurrent.ConcurrentHashMap

class ConnectionManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var eglBaseContext: EglBase.Context? = null
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()
    private var signalingServer: SignalingServer? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    var onRemoteStreamReceived: ((String, MediaStream) -> Unit)? = null
    var onConnectionStateChange: ((String, PeerConnection.IceConnectionState) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        initializeWebRTC()
    }

    private fun initializeWebRTC() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val eglBase = EglBase.create()
        eglBaseContext = eglBase.eglBaseContext

        val peerConnectionFactoryOptions = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(peerConnectionFactoryOptions)
            .createPeerConnectionFactory()
    }

    fun establishConnection(pairingData: PairingData) {
        if (!pairingData.isTokenValid()) {
            onError?.invoke("Invalid or expired security token")
            return
        }

        signalingServer = SignalingServer(
            scope = scope,
            userId = pairingData.userId,
            deviceId = pairingData.deviceId,
            securityToken = pairingData.securityToken,
            serverUrl = pairingData.serverUrl
        ).apply {
            onOfferReceived = { sdp -> handleRemoteOffer(sdp, pairingData.deviceId) }
            onAnswerReceived = { sdp -> handleRemoteAnswer(sdp, pairingData.deviceId) }
            onIceCandidateReceived = { candidate -> handleRemoteIceCandidate(candidate, pairingData.deviceId) }
            onError = { error -> this@ConnectionManager.onError?.invoke(error) }
        }

        signalingServer?.connect()
    }

    private fun createPeerConnection(deviceId: String): PeerConnection? {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            // Add your TURN servers here
            // PeerConnection.IceServer.builder("turn:your-turn-server")
            //     .setUsername("username")
            //     .setPassword("password")
            //     .createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            enableDtlsSrtp = true
        }

        return peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Log.d(TAG, "onSignalingChange: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    scope.launch {
                        onConnectionStateChange?.invoke(deviceId, state)
                        when (state) {
                            PeerConnection.IceConnectionState.CONNECTED -> {
                                _connectionState.value = ConnectionState.Connected(deviceId)
                            }
                            PeerConnection.IceConnectionState.FAILED -> {
                                _connectionState.value = ConnectionState.Error("ICE connection failed")
                                handleIceFailure(deviceId)
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                _connectionState.value = ConnectionState.Disconnected
                            }
                            else -> {}
                        }
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {
                    Log.d(TAG, "onIceConnectionReceivingChange: $receiving")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                    Log.d(TAG, "onIceGatheringChange: $state")
                }

                override fun onIceCandidate(candidate: IceCandidate) {
                    signalingServer?.sendIceCandidate(candidate, deviceId)
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {
                    Log.d(TAG, "onIceCandidatesRemoved: ${candidates.size}")
                }

                override fun onAddStream(stream: MediaStream) {
                    scope.launch {
                        onRemoteStreamReceived?.invoke(deviceId, stream)
                    }
                }

                override fun onRemoveStream(stream: MediaStream) {
                    Log.d(TAG, "onRemoveStream")
                }

                override fun onDataChannel(dataChannel: DataChannel) {
                    Log.d(TAG, "onDataChannel")
                }

                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "onRenegotiationNeeded")
                }

                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                    Log.d(TAG, "onAddTrack")
                }
            }
        )
    }

    private fun handleRemoteOffer(sdp: SessionDescription, fromDeviceId: String) {
        val peerConnection = peerConnections.getOrPut(fromDeviceId) {
            createPeerConnection(fromDeviceId) ?: return
        }

        peerConnection.setRemoteDescription(
            object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {}
                override fun onSetSuccess() {
                    createAndSetLocalAnswer(peerConnection, fromDeviceId)
                }
                override fun onCreateFailure(error: String) {
                    onError?.invoke("Failed to create remote description: $error")
                }
                override fun onSetFailure(error: String) {
                    onError?.invoke("Failed to set remote description: $error")
                }
            },
            sdp
        )
    }

    private fun createAndSetLocalAnswer(peerConnection: PeerConnection, targetDeviceId: String) {
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {}
                    override fun onSetSuccess() {
                        signalingServer?.sendAnswer(sessionDescription, targetDeviceId)
                    }
                    override fun onCreateFailure(error: String) {
                        onError?.invoke("Failed to create local description: $error")
                    }
                    override fun onSetFailure(error: String) {
                        onError?.invoke("Failed to set local description: $error")
                    }
                }, sessionDescription)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                onError?.invoke("Failed to create answer: $error")
            }
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    private fun handleRemoteAnswer(sdp: SessionDescription, fromDeviceId: String) {
        peerConnections[fromDeviceId]?.setRemoteDescription(
            object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {}
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String) {
                    onError?.invoke("Failed to create remote description: $error")
                }
                override fun onSetFailure(error: String) {
                    onError?.invoke("Failed to set remote description: $error")
                }
            },
            sdp
        )
    }

    private fun handleRemoteIceCandidate(candidate: IceCandidate, fromDeviceId: String) {
        peerConnections[fromDeviceId]?.addIceCandidate(candidate)
    }

    private fun handleIceFailure(deviceId: String) {
        // Implement ICE failure recovery logic
        // For example: retry connection with different ICE servers
        disconnectPeer(deviceId)
        // Notify UI about the failure
        onError?.invoke("Connection failed. Retrying...")
        // Implement retry logic here
    }

    fun disconnectPeer(deviceId: String) {
        peerConnections[deviceId]?.apply {
            close()
            dispose()
        }
        peerConnections.remove(deviceId)
    }

    fun release() {
        peerConnections.keys.forEach { disconnectPeer(it) }
        peerConnections.clear()
        signalingServer?.disconnect()
        signalingServer = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
    }

    companion object {
        private const val TAG = "ConnectionManager"
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class Connected(val deviceId: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
} 