package com.example.smarthomeappfinal.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    fun signInWithToken(token: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthState.Loading
                val result = auth.signInWithCustomToken(token).await()
                _uiState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up if needed
    }
} 