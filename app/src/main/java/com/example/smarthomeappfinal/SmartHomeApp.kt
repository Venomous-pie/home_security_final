package com.example.smarthomeappfinal

import android.app.Application
import com.example.smarthomeappfinal.di.ServiceLocator
import com.google.firebase.FirebaseApp

class SmartHomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize ServiceLocator
        ServiceLocator.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ServiceLocator.reset()
    }
} 