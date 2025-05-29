package com.example.smarthomeappfinal.navigation

import android.content.Context
import android.content.Intent
import androidx.navigation.NavController
import com.example.smarthomeappfinal.*
import com.example.smarthomeappfinal.utils.AppMode
import com.example.smarthomeappfinal.utils.PreferencesManager
import java.lang.ref.WeakReference

class NavigationManager private constructor(context: Context) {
    private val contextRef = WeakReference(context.applicationContext)
    private val preferencesManager = PreferencesManager(context.applicationContext)
    
    fun navigateToLogin() {
        contextRef.get()?.startActivity(Intent(contextRef.get(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    fun navigateToRegister() {
        contextRef.get()?.startActivity(Intent(contextRef.get(), RegisterActivity::class.java))
    }

    fun navigateToMain() {
        contextRef.get()?.startActivity(Intent(contextRef.get(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    fun navigateToPairByLogin() {
        contextRef.get()?.startActivity(Intent(contextRef.get(), PairByLoginActivity::class.java))
    }

    fun navigateToPairByQR(userId: String) {
        contextRef.get()?.startActivity(Intent(contextRef.get(), PairByQRActivity::class.java).apply {
            putExtra(PairByQRActivity.EXTRA_USER_ID, userId)
        })
    }

    fun navigateToHome(navController: NavController) {
        navController.navigate(R.id.navigation_home)
    }

    fun navigateToSettings(navController: NavController) {
        navController.navigate(R.id.navigation_settings)
    }

    fun navigateBasedOnMode(navController: NavController, mode: AppMode) {
        when (mode) {
            AppMode.Monitor -> {
                if (navController.currentDestination?.id != R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home)
                }
            }
            AppMode.Camera -> {
                if (navController.currentDestination?.id != R.id.navigation_camera_capture) {
                    navController.navigate(R.id.navigation_camera_capture)
                }
            }
        }
    }

    fun handleBottomNavigation(navController: NavController, itemId: Int): Boolean {
        val currentMode = preferencesManager.getLastSelectedMode()

        return when (itemId) {
            R.id.navigation_home -> {
                navigateBasedOnMode(navController, currentMode)
                true
            }
            R.id.navigation_notifications -> {
                if (navController.currentDestination?.id != R.id.navigation_notifications) {
                    navController.navigate(R.id.navigation_notifications)
                }
                true
            }
            else -> false
        }
    }

    fun handleBottomNavReselection(navController: NavController, itemId: Int) {
        if (itemId == R.id.navigation_home) {
            val currentMode = preferencesManager.getLastSelectedMode()
            navigateBasedOnMode(navController, currentMode)
        }
    }

    companion object {
        @Volatile
        private var instance: NavigationManager? = null

        fun getInstance(context: Context): NavigationManager {
            return instance ?: synchronized(this) {
                instance ?: NavigationManager(context).also { instance = it }
            }
        }
    }
} 