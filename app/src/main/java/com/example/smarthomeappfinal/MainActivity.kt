package com.example.smarthomeappfinal

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smarthomeappfinal.base.BaseActivity
import com.example.smarthomeappfinal.databinding.ActivityMainBinding
import com.example.smarthomeappfinal.navigation.NavigationManager
import com.example.smarthomeappfinal.utils.Constants
import com.example.smarthomeappfinal.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
// Unused ViewModel imports can be removed if they are no longer needed after UI changes
// import com.example.smarthomeappfinal.ui.home.HomeViewModel
// import com.example.smarthomeappfinal.ui.dashboard.DashboardViewModel
// import com.example.smarthomeappfinal.ui.notifications.NotificationsViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var actionBarSpinner: Spinner? = null
    private lateinit var navController: NavController
    private lateinit var navigationManager: NavigationManager
    private lateinit var preferencesManager: PreferencesManager
    private var isSpinnerProgrammaticSelection = false

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        setupToolbar()
        setupNavigation(savedInstanceState)
        setupSpinner()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        navigationManager = NavigationManager(this, navController)

        if (savedInstanceState == null) {
            val graphInflater = navController.navInflater
            val navGraph = graphInflater.inflate(R.navigation.mobile_navigation)
            
            val startDestination = if (preferencesManager.getStartupMode() == Constants.MODE_CAMERA) {
                R.id.navigation_camera_capture
            } else {
                R.id.navigation_home
            }
            navGraph.setStartDestination(startDestination)
            navController.graph = navGraph
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupSpinner() {
        actionBarSpinner = Spinner(this, Spinner.MODE_DROPDOWN)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.viewer_options_array,
            R.layout.action_bar_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        actionBarSpinner?.adapter = adapter

        isSpinnerProgrammaticSelection = true
        actionBarSpinner?.setSelection(preferencesManager.getLastSelectedSpinnerMode(), false)
        isSpinnerProgrammaticSelection = false

        actionBarSpinner?.onItemSelectedListener = createSpinnerListener()
        setupSpinnerInActionBar()
    }

    private fun createSpinnerListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (isSpinnerProgrammaticSelection) {
                isSpinnerProgrammaticSelection = false
                return
            }
            
            preferencesManager.saveSpinnerMode(position)
            navigationManager.navigateBasedOnMode(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun setupSpinnerInActionBar() {
        val params = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.START
        )
        supportActionBar?.setCustomView(actionBarSpinner, params)
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }

    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { item ->
            navigationManager.handleBottomNavigation(item.itemId)
        }

        navView.setOnNavigationItemReselectedListener { item ->
            navigationManager.handleBottomNavReselection(item.itemId)
        }

        setupNavigationListener()
    }

    private fun setupNavigationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_camera_capture -> {
                    configureAppBarForSpinner(false)
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    configureAppBarForSpinner(true)
                    configureAppBarForTitle(false, null)
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }
    }

    fun configureAppBarForSpinner(showSpinner: Boolean) {
        actionBarSpinner?.visibility = if (showSpinner) View.VISIBLE else View.GONE
        supportActionBar?.setDisplayShowCustomEnabled(showSpinner)
    }

    fun configureAppBarForTitle(showTitle: Boolean, title: CharSequence?) {
        supportActionBar?.setDisplayShowTitleEnabled(showTitle)
        supportActionBar?.title = if (showTitle) title else null
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun loadAndApplyTheme() {
        val prefs = getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    // Removed onCreateOptionsMenu and onOptionsItemSelected as they are not directly relevant to spinner/drawer issue
    // and spinner item selection is handled directly.
}