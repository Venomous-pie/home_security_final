package com.example.smarthomeappfinal.ui.scan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentScanQrBinding
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
class ScanQrFragment : Fragment() {
    private var _binding: FragmentScanQrBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanQrViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var barcodeScanner: BarcodeScanner

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBarcodeScanner()
        setupCameraExecutor()
        observeUiState()
        setupClickListeners()

        // Request camera permission
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun setupBarcodeScanner() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    private fun setupCameraExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUiForState(state)
                }
            }
        }
    }

    private fun updateUiForState(state: ScanUiState) {
        binding.progressBar.visibility = View.GONE
        binding.btnPermission.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        when (state) {
            is ScanUiState.RequestPermission -> {
                binding.btnPermission.visibility = View.VISIBLE
            }
            is ScanUiState.Scanning -> {
                startCamera()
            }
            is ScanUiState.Success -> {
                binding.progressBar.visibility = View.VISIBLE
                findNavController().navigate(R.id.navigation_monitor)
            }
            is ScanUiState.Error -> {
                handleError(state.error)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPermission.setOnClickListener {
            when {
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showPermissionRationale()
                }
                else -> {
                    openAppSettings()
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        try {
            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        @androidx.camera.core.ExperimentalGetImage
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (barcodes.isNotEmpty()) {
                                        barcodes[0].rawValue?.let { content ->
                                            viewModel.onQrCodeDetected(content)
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    handleError(ScanError.InvalidQr)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            handleError(ScanError.NetworkFailure)
        }
    }

    private fun handleError(error: ScanError) {
        val message = when (error) {
            ScanError.PermissionDenied -> getString(R.string.camera_permission_denied_message)
            ScanError.InvalidQr -> getString(R.string.error_invalid_qr)
            ScanError.TokenExpired -> getString(R.string.error_token_expired)
            ScanError.NetworkFailure -> getString(R.string.error_network)
        }

        binding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }

        if (error == ScanError.PermissionDenied) {
            binding.btnPermission.visibility = View.VISIBLE
        }
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            binding.root,
            R.string.camera_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.button_grant_permission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }.show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
        imageAnalysis = null
        cameraExecutor.shutdown()
        _binding = null
    }
} 