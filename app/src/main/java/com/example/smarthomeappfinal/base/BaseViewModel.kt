package com.example.smarthomeappfinal.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected fun launchIO(
        showLoading: Boolean = true,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                if (showLoading) _loading.value = true
                block()
            } catch (e: Exception) {
                _error.emit(e.message ?: "An error occurred")
            } finally {
                if (showLoading) _loading.value = false
            }
        }
    }

    protected fun launchMain(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        launchIO(showLoading, Dispatchers.Main, block)
    }

    protected suspend fun emitError(message: String) {
        _error.emit(message)
    }

    protected fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }
} 