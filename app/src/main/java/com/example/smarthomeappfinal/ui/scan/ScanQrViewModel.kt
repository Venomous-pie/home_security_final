package com.example.smarthomeappfinal.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeappfinal.webrtc.PairingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanQrViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.RequestPermission)
    val uiState: StateFlow<ScanUiState> = _uiState

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

                // Store the paired device information
                storePairedDevice(pairingData)
                
                _uiState.value = ScanUiState.Success
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(ScanError.NetworkFailure)
            }
        }
    }

    private fun storePairedDevice(pairingData: PairingData) {
        // TODO: Store the paired device information in your preferred storage mechanism
        // This could be Room database, SharedPreferences, or a remote backend
        // For now, we'll just emit success
    }

    fun resetToScanning() {
        _uiState.value = ScanUiState.Scanning
    }
}

sealed class ScanUiState {
    object RequestPermission : ScanUiState()
    object Scanning : ScanUiState()
    object Success : ScanUiState()
    data class Error(val error: ScanError) : ScanUiState()
}

sealed class ScanError {
    object PermissionDenied : ScanError()
    object InvalidQr : ScanError()
    object TokenExpired : ScanError()
    object NetworkFailure : ScanError()
} 