package com.example.smarthomeappfinal.ui.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeappfinal.data.model.DevicePairing
import com.example.smarthomeappfinal.data.repository.DevicePairingRepository
import com.example.smarthomeappfinal.repository.DeviceRepository
import com.example.smarthomeappfinal.network.NetworkResult
import com.example.smarthomeappfinal.utils.Constants
import com.example.smarthomeappfinal.webrtc.PairingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ScanQrViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.RequestPermission)
    val uiState: StateFlow<ScanUiState> = _uiState
    private var appMode: Int = Constants.AppMode.MODE_MONITOR
    private val repository = DevicePairingRepository()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(Constants.AppMode.APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
        appMode = prefs.getInt(Constants.AppMode.KEY_STARTUP_MODE, Constants.AppMode.MODE_MONITOR)
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = if (isGranted) {
            ScanUiState.Scanning
        } else {
            ScanUiState.Error(ScanError.PermissionDenied)
        }
    }

    fun onQrCodeDetected(content: String) {
        viewModelScope.launch {
            try {
                val pairingData = PairingData.fromQrContent(content) ?: run {
                    _uiState.value = ScanUiState.Error(ScanError.InvalidQr)
                    return@launch
                }

                if (!pairingData.isTokenValid()) {
                    _uiState.value = ScanUiState.Error(ScanError.TokenExpired)
                    return@launch
                }

                // Generate a new pair code
                val generatedPairCode = generatePairCode()

                // Get or create device pairing in Firebase
                val existingPairing = repository.getPairing(generatedPairCode)
                if (existingPairing == null) {
                    // Create new pairing
                    val devicePairing = DevicePairing(
                        deviceId = UUID.randomUUID().toString(),
                        userId = pairingData.userId,
                        mode = appMode,
                        pairCode = generatedPairCode,
                        deviceName = android.os.Build.MODEL
                    )
                    repository.createPairing(devicePairing)

                    // Add device to the list using DeviceRepository
                    val deviceRepository = DeviceRepository()
                    deviceRepository.pairDevice(
                        deviceId = devicePairing.deviceId,
                        type = if (appMode == Constants.AppMode.MODE_MONITOR) "monitor" else "camera",
                        name = devicePairing.deviceName
                    ).collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                // Navigate based on app mode
                                _uiState.value = ScanUiState.Success(appMode)
                            }
                            is NetworkResult.Error -> {
                                _uiState.value = ScanUiState.Error(ScanError.NetworkFailure)
                            }
                            is NetworkResult.Loading -> {
                                // Loading state is handled by the UI
                            }
                        }
                    }
                } else {
                    // Update existing pairing with new mode
                    repository.updatePairingMode(generatedPairCode, appMode)
                    _uiState.value = ScanUiState.Success(appMode)
                }
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(ScanError.NetworkFailure)
            }
        }
    }

    private fun generatePairCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { allowedChars.random() }.joinToString("")
    }

    fun resetToScanning() {
        _uiState.value = ScanUiState.Scanning
    }
}

sealed class ScanUiState {
    object RequestPermission : ScanUiState()
    object Scanning : ScanUiState()
    data class Success(val appMode: Int) : ScanUiState()
    data class Error(val error: ScanError) : ScanUiState()
}

sealed class ScanError {
    object PermissionDenied : ScanError()
    object InvalidQr : ScanError()
    object TokenExpired : ScanError()
    object NetworkFailure : ScanError()
} 