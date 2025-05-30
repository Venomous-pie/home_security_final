package com.example.smarthomeappfinal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smarthomeappfinal.base.BaseActivity
import com.example.smarthomeappfinal.databinding.ActivityMainBinding
import com.example.smarthomeappfinal.navigation.NavigationManager
import com.example.smarthomeappfinal.utils.AppMode
import com.example.smarthomeappfinal.utils.PreferencesManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var actionBarSpinner: Spinner? = null
    private lateinit var navController: NavController
    private lateinit var navigationManager: NavigationManager
    override lateinit var preferencesManager: PreferencesManager
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var firebaseAuth: FirebaseAuth
    
    // State management
    private var isSpinnerInitialized = false
    private var savedSpinnerPosition: Int? = null

    // SharedPreferences keys for theme
    private val THEME_PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "selected_theme"

    // SharedPreferences keys for startup mode
    private val APP_MODE_PREFS_NAME = "app_mode_prefs"
    private val KEY_STARTUP_MODE = "startup_mode"
    private val KEY_LAST_SELECTED_SPINNER_MODE = "last_selected_spinner_mode"
    private val MODE_MONITOR = 0
    private val MODE_CAMERA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferencesManager(this)
        firebaseAuth = FirebaseAuth.getInstance()
        savedInstanceState?.getInt(KEY_SPINNER_POSITION)
        setupComponents()
    }

    private fun setupComponents() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        setupNavigation()
        setupBottomNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        navController = navHostFragment.navController
        navigationManager = NavigationManager.getInstance(this)

        val startDestination = if (preferencesManager.getStartupMode() == AppMode.Camera) {
            R.id.navigation_camera_capture
        } else {
            R.id.navigation_home
        }
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation).apply {
            setStartDestination(startDestination)
        }
        navController.graph = navGraph

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_camera_capture,
                R.id.navigation_monitor
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupBottomNavigation() {
        binding.navView.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { item ->
                navigationManager.handleBottomNavigation(navController, item.itemId)
            }
            setOnItemReselectedListener { item -> 
                navigationManager.handleBottomNavReselection(navController, item.itemId)
            }
        }
        setupNavigationListener()
    }

    private fun setupNavigationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Handle bottom nav visibility
            binding.navView.visibility = if (destination.id == R.id.navigation_camera_capture) {
                View.GONE
            } else {
                View.VISIBLE
            }

            // Remove spinner visibility logic since it's redundant
            actionBarSpinner?.visibility = View.GONE
        }
    }

    fun configureAppBarForSpinner(showSpinner: Boolean) {
        // Always hide spinner since we're removing this feature
        actionBarSpinner?.visibility = View.GONE
        supportActionBar?.setDisplayShowCustomEnabled(false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false // No longer needed since drawer is removed
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val KEY_SPINNER_POSITION = "spinner_position"
    }
}