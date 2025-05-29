package com.example.smarthomeappfinal.utils

object Constants {
    object AppMode {
        const val MODE_MONITOR = 0
        const val MODE_CAMERA = 1
        const val APP_MODE_PREFS_NAME = "app_mode_prefs"
        const val KEY_STARTUP_MODE = "startup_mode"
        const val KEY_LAST_SELECTED_SPINNER_MODE = "last_selected_spinner_mode"
    }

    object Preferences {
        object Theme {
            const val THEME_PREFS_NAME = "theme_prefs"
            const val KEY_THEME = "selected_theme"
        }
    }

    object Auth {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PASSWORD_LENGTH = 30
    }

    object Navigation {
        const val DEEP_LINK_SCHEME = "smarthome"
        const val DEEP_LINK_HOST = "app"
    }

    object Network {
        const val TIMEOUT_CONNECT = 30L
        const val TIMEOUT_READ = 30L
        const val TIMEOUT_WRITE = 30L
    }

    object QRCode {
        const val VALIDITY_DURATION = 30 * 60 * 1000L // 30 minutes in milliseconds
        const val QR_CODE_SIZE = 600
    }
} 