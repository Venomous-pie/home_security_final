package com.example.smarthomeappfinal.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val throwable: Throwable) : AuthState()
} 