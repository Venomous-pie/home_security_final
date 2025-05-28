package com.example.smarthomeappfinal.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val appModePrefs: SharedPreferences = context.getSharedPreferences(Constants.APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
    private val themePrefs: SharedPreferences = context.getSharedPreferences(Constants.THEME_PREFS_NAME, Context.MODE_PRIVATE)

    fun getStartupMode(): Int = appModePrefs.getInt(Constants.KEY_STARTUP_MODE, Constants.MODE_MONITOR)

    fun getLastSelectedSpinnerMode(): Int = appModePrefs.getInt(Constants.KEY_LAST_SELECTED_SPINNER_MODE, getStartupMode())

    fun saveSpinnerMode(mode: Int) {
        appModePrefs.edit().apply {
            putInt(Constants.KEY_LAST_SELECTED_SPINNER_MODE, mode)
            putInt(Constants.KEY_STARTUP_MODE, mode)
            apply()
        }
    }

    fun getTheme(): Int = themePrefs.getInt(Constants.KEY_THEME, -1)

    fun saveTheme(theme: Int) {
        themePrefs.edit().putInt(Constants.KEY_THEME, theme).apply()
    }
} 