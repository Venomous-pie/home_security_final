package com.example.smarthomeappfinal.base

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.smarthomeappfinal.utils.AppTheme
import com.example.smarthomeappfinal.utils.Constants
import com.example.smarthomeappfinal.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {
    protected open lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager(this)
        loadAndApplyTheme()
        super.onCreate(savedInstanceState)
    }

    protected fun loadAndApplyTheme() {
        val theme = preferencesManager.getTheme()
        val nightMode = when (theme) {
            AppTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    protected fun setTheme(theme: AppTheme) {
        preferencesManager.saveTheme(theme)
        loadAndApplyTheme()
    }

    protected fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            try {
                block()
            } catch (e: Exception) {
                showError(e.message ?: "An error occurred")
            }
        }
    }
} 