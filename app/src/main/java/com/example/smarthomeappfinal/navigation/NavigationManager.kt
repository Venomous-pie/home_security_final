package com.example.smarthomeappfinal.navigation

import android.content.Context
import androidx.navigation.NavController
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.utils.Constants

class NavigationManager(private val context: Context, private val navController: NavController) {
    
    fun navigateBasedOnMode(mode: Int) {
        when (mode) {
            Constants.MODE_MONITOR -> {
                if (navController.currentDestination?.id != R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home)
                }
            }
            Constants.MODE_CAMERA -> {
                if (navController.currentDestination?.id != R.id.navigation_camera_capture) {
                    navController.navigate(R.id.navigation_camera_capture)
                }
            }
        }
    }

    fun handleBottomNavigation(itemId: Int): Boolean {
        val currentAppModePrefs = context.getSharedPreferences(Constants.APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
        val currentSelectedMode = currentAppModePrefs.getInt(Constants.KEY_LAST_SELECTED_SPINNER_MODE, Constants.MODE_MONITOR)

        return when (itemId) {
            R.id.navigation_home -> {
                if (currentSelectedMode == Constants.MODE_CAMERA) {
                    navigateBasedOnMode(Constants.MODE_CAMERA)
                } else {
                    navigateBasedOnMode(Constants.MODE_MONITOR)
                }
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

    fun handleBottomNavReselection(itemId: Int) {
        if (itemId == R.id.navigation_home) {
            val currentAppModePrefs = context.getSharedPreferences(Constants.APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
            val currentSelectedMode = currentAppModePrefs.getInt(Constants.KEY_LAST_SELECTED_SPINNER_MODE, Constants.MODE_MONITOR)
            navigateBasedOnMode(currentSelectedMode)
        }
    }
} 