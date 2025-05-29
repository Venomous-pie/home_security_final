package com.example.smarthomeappfinal.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

sealed class AppMode(val value: Int) {
    object Monitor : AppMode(0)
    object Camera : AppMode(1)

    companion object {
        fun fromInt(value: Int) = when(value) {
            0 -> Monitor
            1 -> Camera
            else -> Monitor
        }
    }
}

sealed class AppTheme(val value: Int) {
    object System : AppTheme(-1)
    object Light : AppTheme(1)
    object Dark : AppTheme(2)

    companion object {
        fun fromInt(value: Int) = when(value) {
            1 -> Light
            2 -> Dark
            else -> System
        }
    }
}

class PreferencesManager(context: Context) {
    private val appModePrefs: SharedPreferences = context.getSharedPreferences(Constants.AppMode.APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
    private val themePrefs: SharedPreferences = context.getSharedPreferences(Constants.Preferences.Theme.THEME_PREFS_NAME, Context.MODE_PRIVATE)

    fun getStartupMode(): AppMode = 
        AppMode.fromInt(appModePrefs.getInt(Constants.AppMode.KEY_STARTUP_MODE, AppMode.Monitor.value))

    fun getLastSelectedMode(): AppMode = 
        AppMode.fromInt(appModePrefs.getInt(Constants.AppMode.KEY_LAST_SELECTED_SPINNER_MODE, getStartupMode().value))

    fun saveAppMode(mode: AppMode) {
        appModePrefs.edit {
            putInt(Constants.AppMode.KEY_LAST_SELECTED_SPINNER_MODE, mode.value)
            putInt(Constants.AppMode.KEY_STARTUP_MODE, mode.value)
        }
    }

    fun getTheme(): AppTheme = 
        AppTheme.fromInt(themePrefs.getInt(Constants.Preferences.Theme.KEY_THEME, AppTheme.System.value))

    fun saveTheme(theme: AppTheme) {
        themePrefs.edit {
            putInt(Constants.Preferences.Theme.KEY_THEME, theme.value)
        }
    }
} 