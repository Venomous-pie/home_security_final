package com.example.smarthomeappfinal.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.smarthomeappfinal.utils.Constants

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        loadAndApplyTheme()
        super.onCreate(savedInstanceState)
    }

    protected fun loadAndApplyTheme() {
        val prefs = getSharedPreferences(Constants.THEME_PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getInt(Constants.KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    protected fun setTheme(theme: Int) {
        val prefs = getSharedPreferences(Constants.THEME_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(Constants.KEY_THEME, theme).apply()
        AppCompatDelegate.setDefaultNightMode(theme)
    }
} 