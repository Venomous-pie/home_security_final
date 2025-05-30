package com.example.smarthomeappfinal.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.TextureView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentCameraPreviewBinding
import com.example.smarthomeappfinal.network.NetworkResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.PeerConnection

class CameraPreviewFragment : Fragment(), TextureView.SurfaceTextureListener {

    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!
    
    private var cameraModule: CameraModule? = null
    private var eglBaseContext: EglBase.Context? = null
    private var isFullScreen = false
    private var windowInsetsController: WindowInsetsControllerCompat? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCameraPreview()
        } else {
            showPermissionError()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupWindowInsets()
        initializeCamera()
        setupUI()
        observeCameraState()
    }

    private fun setupWindowInsets() {
        activity?.window?.let { window ->
            windowInsetsController = WindowInsetsControllerCompat(window, binding.root)
        }
    }

    private fun initializeCamera() {
        val eglBase = EglBase.create()
        eglBaseContext = eglBase.eglBaseContext
        
        binding.textureView.surfaceTextureListener = this
        cameraModule = CameraModule(requireContext(), eglBaseContext!!)
    }

    private fun setupUI() {
        binding.btnFullscreen.setOnClickListener {
            toggleFullScreen()
        }

        binding.btnRetry.setOnClickListener {
            checkPermissionAndStartPreview()
        }
    }

    private fun observeCameraState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraModule?.cameraState?.collectLatest { state ->
                    when (state) {
                        is CameraState.Error -> showError(state.message)
                        is CameraState.Reconnecting -> showReconnecting()
                        is CameraState.Streaming -> showStreaming()
                        is CameraState.Initialized -> Unit // No UI update needed
                    }
                }
            }
        }
    }

    private fun checkPermissionAndStartPreview() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCameraPreview()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCameraPreview() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = cameraModule?.startPreview(binding.textureView)) {
                is NetworkResult.Success -> {
                    binding.groupError.visibility = View.GONE
                    binding.btnFullscreen.visibility = View.VISIBLE
                }
                is NetworkResult.Error -> {
                    showError(result.message)
                }
                else -> Unit
            }
        }
    }

    private fun toggleFullScreen() {
        if (!isFullScreenSupported()) {
            showFullScreenUnsupportedMessage()
            return
        }

        isFullScreen = !isFullScreen
        
        if (isFullScreen) {
            enterFullScreen()
        } else {
            exitFullScreen()
        }
        
        binding.btnFullscreen.setImageResource(
            if (isFullScreen) R.drawable.ic_fullscreen_exit
            else R.drawable.ic_fullscreen
        )
    }

    private fun isFullScreenSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    private fun enterFullScreen() {
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }

    private fun exitFullScreen() {
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
    }

    private fun showFullScreenUnsupportedMessage() {
        Snackbar.make(
            binding.root,
            getString(R.string.fullscreen_not_supported),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showError(message: String) {
        binding.groupError.visibility = View.VISIBLE
        binding.btnFullscreen.visibility = View.GONE
        binding.textError.text = message
    }

    private fun showReconnecting() {
        Snackbar.make(
            binding.root,
            getString(R.string.reconnecting),
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    private fun showStreaming() {
        binding.groupError.visibility = View.GONE
        binding.btnFullscreen.visibility = View.VISIBLE
    }

    private fun showPermissionError() {
        showError(getString(R.string.camera_permission_denied))
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            binding.root,
            getString(R.string.camera_permission_rationale),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.grant_permission)) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exitFullScreen()
        cameraModule?.release()
        _binding = null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        checkPermissionAndStartPreview()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Handle size changes if needed
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        // Return true if the TextureView can release the SurfaceTexture
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Called when the content of the surface has changed
    }
} 