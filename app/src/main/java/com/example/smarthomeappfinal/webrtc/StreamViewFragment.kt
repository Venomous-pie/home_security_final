package com.example.smarthomeappfinal.webrtc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentStreamViewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class StreamViewFragment : Fragment() {

    private var _binding: FragmentStreamViewBinding? = null
    private val binding get() = _binding!!

    private var connectionManager: ConnectionManager? = null
    private var eglBaseContext: EglBase.Context? = null
    private val renderers = mutableMapOf<String, SurfaceViewRenderer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeWebRTC()
        setupUI()
        observeConnectionState()
    }

    private fun initializeWebRTC() {
        val eglBase = EglBase.create()
        eglBaseContext = eglBase.eglBaseContext

        connectionManager = ConnectionManager(requireContext(), viewLifecycleOwner.lifecycleScope).apply {
            onRemoteStreamReceived = { deviceId, stream -> handleRemoteStream(deviceId, stream) }
            onConnectionStateChange = { deviceId, state -> handleConnectionStateChange(deviceId, state) }
            onError = { error -> showError(error) }
        }

        // Initialize the main renderer
        setupRenderer(binding.mainRenderer)
    }

    private fun setupUI() {
        binding.btnRetry.setOnClickListener {
            retryConnection()
        }

        binding.btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }
    }

    private fun observeConnectionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                connectionManager?.connectionState?.collectLatest { state ->
                    when (state) {
                        is ConnectionState.Connected -> {
                            showConnectedState()
                        }
                        is ConnectionState.Disconnected -> {
                            showDisconnectedState()
                        }
                        is ConnectionState.Error -> {
                            showErrorState(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupRenderer(renderer: SurfaceViewRenderer) {
        renderer.apply {
            init(eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            setEnableHardwareScaler(true)
            setMirror(false)
        }
    }

    private fun handleRemoteStream(deviceId: String, stream: MediaStream) {
        if (stream.videoTracks.isEmpty()) {
            showError("No video track available")
            return
        }

        val renderer = getOrCreateRenderer(deviceId)
        stream.videoTracks[0].addSink(renderer)
    }

    private fun getOrCreateRenderer(deviceId: String): SurfaceViewRenderer {
        return renderers.getOrPut(deviceId) {
            when {
                renderers.isEmpty() -> binding.mainRenderer
                else -> createAdditionalRenderer(deviceId)
            }
        }
    }

    private fun createAdditionalRenderer(deviceId: String): SurfaceViewRenderer {
        return SurfaceViewRenderer(requireContext()).also { renderer ->
            setupRenderer(renderer)
            binding.additionalRenderersContainer.addView(renderer)
            renderers[deviceId] = renderer
        }
    }

    private fun handleConnectionStateChange(deviceId: String, state: PeerConnection.IceConnectionState) {
        when (state) {
            PeerConnection.IceConnectionState.CONNECTED -> {
                binding.connectionStatus.text = getString(R.string.status_connected)
                binding.connectionStatus.setBackgroundResource(R.drawable.bg_status_connected)
            }
            PeerConnection.IceConnectionState.DISCONNECTED,
            PeerConnection.IceConnectionState.FAILED -> {
                binding.connectionStatus.text = getString(R.string.status_disconnected)
                binding.connectionStatus.setBackgroundResource(R.drawable.bg_status_error)
            }
            PeerConnection.IceConnectionState.CHECKING -> {
                binding.connectionStatus.text = getString(R.string.status_connecting)
                binding.connectionStatus.setBackgroundResource(R.drawable.bg_status_connecting)
            }
            else -> {}
        }
    }

    private fun showConnectedState() {
        binding.apply {
            progressBar.isVisible = false
            mainRenderer.isVisible = true
            errorGroup.isVisible = false
            btnFullscreen.isVisible = true
            connectionStatus.isVisible = true
        }
    }

    private fun showDisconnectedState() {
        binding.apply {
            progressBar.isVisible = false
            mainRenderer.isVisible = false
            errorGroup.isVisible = true
            btnFullscreen.isVisible = false
            connectionStatus.isVisible = false
            textError.text = getString(R.string.connection_lost)
        }
    }

    private fun showErrorState(message: String) {
        binding.apply {
            progressBar.isVisible = false
            mainRenderer.isVisible = false
            errorGroup.isVisible = true
            btnFullscreen.isVisible = false
            connectionStatus.isVisible = false
            textError.text = message
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun retryConnection() {
        // Implement retry logic
    }

    private var isFullscreen = false
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        // Implement fullscreen toggle logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderers.values.forEach { renderer ->
            renderer.release()
        }
        renderers.clear()
        connectionManager?.release()
        connectionManager = null
        _binding = null
    }

    companion object {
        private const val TAG = "StreamViewFragment"

        fun newInstance(pairingData: PairingData): StreamViewFragment {
            return StreamViewFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", pairingData.userId)
                    putString("deviceId", pairingData.deviceId)
                    putString("securityToken", pairingData.securityToken)
                    putString("serverUrl", pairingData.serverUrl)
                    putString("roomId", pairingData.roomId)
                }
            }
        }
    }
} 