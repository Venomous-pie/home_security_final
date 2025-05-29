package com.example.smarthomeappfinal.di

import android.content.Context
import com.example.smarthomeappfinal.auth.AuthManager
import com.example.smarthomeappfinal.navigation.NavigationManager
import com.example.smarthomeappfinal.network.NetworkModule
import com.example.smarthomeappfinal.network.SmartHomeApiService
import com.example.smarthomeappfinal.repository.DeviceRepository
import com.example.smarthomeappfinal.utils.PreferencesManager
import java.lang.ref.WeakReference

object ServiceLocator {
    private var contextRef: WeakReference<Context>? = null
    private var preferencesManager: PreferencesManager? = null

    fun init(appContext: Context) {
        contextRef = WeakReference(appContext.applicationContext)
    }

    fun providePreferencesManager(): PreferencesManager {
        return preferencesManager ?: synchronized(this) {
            preferencesManager ?: createPreferencesManager().also {
                preferencesManager = it
            }
        }
    }

    fun provideDeviceRepository(): DeviceRepository {
        return DeviceRepository.getInstance()
    }

    private fun createPreferencesManager(): PreferencesManager {
        val appContext = checkNotNull(contextRef?.get()) { "Call init(Context) before using ServiceLocator" }
        return PreferencesManager(appContext)
    }

    fun reset() {
        contextRef?.clear()
        contextRef = null
        preferencesManager = null
        NetworkModule.reset()
    }
} 