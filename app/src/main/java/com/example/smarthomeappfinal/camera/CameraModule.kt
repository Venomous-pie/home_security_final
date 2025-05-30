package com.example.smarthomeappfinal.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import com.example.smarthomeappfinal.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.webrtc.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraModule(
    private val context: Context,
    private val eglBaseContext: EglBase.Context
) {
    companion object {
        private const val TAG = "CameraModule"
        private const val CAMERA_FRONT = "1"
        private const val CAMERA_BACK = "0"
        private const val MAX_RETRY_COUNT = 3
        private const val RECONNECT_DELAY_MS = 2000L
    }

    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var isStreaming = false
    private var retryCount = 0

    // State management
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initialized)
    val cameraState: Flow<CameraState> = _cameraState

    init {
        initializeCameraThread()
        initializeWebRTC()
        initializeImageReader()
    }

    private fun initializeCameraThread() {
        cameraThread = HandlerThread("CameraThread").apply { start() }
        cameraHandler = Handler(cameraThread!!.looper)
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun initializeWebRTC() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val peerConnectionFactoryOptions = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(peerConnectionFactoryOptions)
            .createPeerConnectionFactory()

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer = createCameraCapturer()
        localVideoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
    }

    private fun initializeImageReader() {
        imageReader = ImageReader.newInstance(
            1920, 1080, // You can adjust these dimensions based on your needs
            ImageFormat.YUV_420_888,
            2 // Maximum number of images that can be accessed in memory at once
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image: Image? = reader.acquireLatestImage()
                try {
                    // Process the image here if needed
                    // For example, convert to WebRTC frame
                } finally {
                    image?.close()
                }
            }, cameraHandler)
        }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        return enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }?.let { 
            enumerator.createCapturer(it, null) 
        } ?: enumerator.deviceNames.firstOrNull { enumerator.isBackFacing(it) }?.let {
            enumerator.createCapturer(it, null)
        }
    }

    suspend fun startPreview(textureView: TextureView): NetworkResult<Unit> = suspendCoroutine { continuation ->
        if (!hasRequiredPermissions()) {
            continuation.resume(NetworkResult.Error("Camera permissions not granted"))
            return@suspendCoroutine
        }

        try {
            val cameraId = CAMERA_BACK
            val characteristics = cameraManager?.getCameraCharacteristics(cameraId)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val previewSize = map?.getOutputSizes(SurfaceTexture::class.java)?.maxByOrNull { 
                it.width * it.height 
            } ?: Size(1920, 1080)

            textureView.surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(textureView.surfaceTexture)

            cameraManager?.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession(surface)
                    continuation.resume(NetworkResult.Success(Unit))
                }

                override fun onDisconnected(camera: CameraDevice) {
                    closeCamera()
                    continuation.resume(NetworkResult.Error("Camera disconnected"))
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    closeCamera()
                    continuation.resume(NetworkResult.Error("Camera error: $error"))
                }
            }, cameraHandler)
        } catch (e: CameraAccessException) {
            continuation.resume(NetworkResult.Error("Failed to access camera: ${e.message}"))
        } catch (e: SecurityException) {
            continuation.resume(NetworkResult.Error("Camera permission denied: ${e.message}"))
        }
    }

    private fun createCaptureSession(surface: Surface) {
        try {
            val previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surface)

            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                previewRequestBuilder?.build()!!,
                                null,
                                cameraHandler
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "Failed to start camera preview: ${e.message}")
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Failed to configure camera session")
                    }
                },
                cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to create capture session: ${e.message}")
        }
    }

    fun startStreaming(iceServers: List<PeerConnection.IceServer>, onIceCandidate: (IceCandidate) -> Unit) {
        if (isStreaming) return

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    onIceCandidate(candidate)
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {}
                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    when (state) {
                        PeerConnection.IceConnectionState.DISCONNECTED,
                        PeerConnection.IceConnectionState.FAILED -> {
                            handleConnectionFailure()
                        }
                        else -> {}
                    }
                }
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
                override fun onAddStream(stream: MediaStream) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(dataChannel: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
            }
        )

        localVideoTrack = peerConnectionFactory?.createVideoTrack("video", localVideoSource)
        peerConnection?.addTrack(localVideoTrack)
        videoCapturer?.startCapture(1280, 720, 30)
        isStreaming = true
        _cameraState.value = CameraState.Streaming
    }

    private fun handleConnectionFailure() {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++
            _cameraState.value = CameraState.Reconnecting
            Handler().postDelayed({
                // Implement reconnection logic
                retryCount = 0
            }, RECONNECT_DELAY_MS)
        } else {
            _cameraState.value = CameraState.Error("Connection failed after $MAX_RETRY_COUNT retries")
            stopStreaming()
        }
    }

    fun stopStreaming() {
        isStreaming = false
        videoCapturer?.stopCapture()
        localVideoTrack?.dispose()
        localVideoSource?.dispose()
        _cameraState.value = CameraState.Initialized
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun closeCamera() {
        try {
            captureSession?.apply {
                close()
                captureSession = null
            }
            
            cameraDevice?.apply {
                close()
                cameraDevice = null
            }
            
            imageReader?.apply {
                close()
                imageReader = null
            }
            
            stopStreaming()
            
            cameraThread?.quitSafely()
            cameraThread = null
            cameraHandler = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing camera: ${e.message}")
        }
    }

    fun release() {
        closeCamera()
        surfaceTextureHelper?.dispose()
        videoCapturer?.dispose()
        peerConnectionFactory?.dispose()
    }
}

sealed class CameraState {
    object Initialized : CameraState()
    object Streaming : CameraState()
    object Reconnecting : CameraState()
    data class Error(val message: String) : CameraState()
} 