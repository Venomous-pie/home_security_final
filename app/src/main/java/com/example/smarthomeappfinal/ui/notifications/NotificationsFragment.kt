package com.example.smarthomeappfinal.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smarthomeappfinal.LoginActivity
import com.example.smarthomeappfinal.MainActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentNotificationsBinding
import com.example.smarthomeappfinal.navigation.NavigationManager
import com.example.smarthomeappfinal.utils.AppMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationManager: NavigationManager

    // App Mode SharedPreferences (also used by MainActivity for spinner)
    private val APP_MODE_PREFS_NAME = "app_mode_prefs" 
    private val KEY_STARTUP_MODE = "startup_mode"
    private val KEY_LAST_SELECTED_SPINNER_MODE = "last_selected_spinner_mode"
    private val MODE_MONITOR = 0
    private val MODE_CAMERA = 1

    // Theme SharedPreferences
    private val THEME_PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "selected_theme"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        navigationManager = NavigationManager.getInstance(requireContext())
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        binding.tvUserEmail.text = currentUser?.email ?: getString(R.string.default_user_email)
        binding.tvUserName.text = currentUser?.displayName ?: getString(R.string.default_user_name)

        loadAndApplyTheme()
        updateCurrentThemeTextView()
        updateStartupModeButtonText()
        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        binding.userInfoSection.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_navigation_account)
        }

        binding.itemDeviceManagement.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_device_management)
        }

        binding.itemSwitchToCamera.setOnClickListener {
            handleModeSwitchRequest()
        }

        binding.itemViewerName.setOnClickListener {
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            Toast.makeText(context, "Device: $deviceName", Toast.LENGTH_LONG).show()
        }

        binding.itemTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_logout_title))
            .setMessage(getString(R.string.confirm_logout_message))
            .setPositiveButton(getString(R.string.yes_action)) { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no_action)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        firebaseAuth.signOut()
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun updateStartupModeButtonText() {
        val sharedPreferences = requireActivity().getSharedPreferences(APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = sharedPreferences.getInt(KEY_STARTUP_MODE, MODE_MONITOR)
        if (currentMode == MODE_CAMERA) {
            binding.tvSwitchModeLabel.text = getString(R.string.switch_to_monitor_mode_label)
        } else {
            binding.tvSwitchModeLabel.text = getString(R.string.switch_to_camera_mode_label)
        }
    }

    private fun handleModeSwitchRequest() {
        val sharedPreferences = requireActivity().getSharedPreferences(APP_MODE_PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = sharedPreferences.getInt(KEY_STARTUP_MODE, MODE_MONITOR)
        val newMode = if (currentMode == MODE_MONITOR) MODE_CAMERA else MODE_MONITOR
        val newModeAppMode = if (newMode == MODE_CAMERA) AppMode.Camera else AppMode.Monitor

        // Save the new mode
        with(sharedPreferences.edit()) {
            putInt(KEY_STARTUP_MODE, newMode)
            putInt(KEY_LAST_SELECTED_SPINNER_MODE, newMode)
            apply()
        }
        
        // Update UI
        updateStartupModeButtonText()
        
        // Navigate to home with new mode
        navigationManager.navigateBasedOnMode(findNavController(), newModeAppMode)
    }

    // --- Existing Theme Methods (unchanged) ---
    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system_default)
        )
        val modes = arrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        val themePrefs = requireActivity().getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        val currentNightMode = themePrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        var checkedItem = modes.indexOf(currentNightMode)
        if (checkedItem == -1) checkedItem = 2

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme_header))
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedMode = modes[which]
                saveThemePreference(selectedMode)
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                updateCurrentThemeTextView()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveThemePreference(themeMode: Int) {
        val themePrefs = requireActivity().getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        with(themePrefs.edit()) {
            putInt(KEY_THEME, themeMode)
            apply()
        }
    }

    private fun loadAndApplyTheme() {
        val themePrefs = requireActivity().getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        val selectedTheme = themePrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(selectedTheme)
    }

    private fun updateCurrentThemeTextView() {
        val themePrefs = requireActivity().getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        val currentNightMode = themePrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val themeText = when (currentNightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system_default)
        }
        binding.tvCurrentTheme.text = themeText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}