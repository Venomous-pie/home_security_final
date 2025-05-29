package com.example.smarthomeappfinal

import android.content.Context
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
import androidx.core.content.ContextCompat // For loading drawable
// Unused ViewModel imports can be removed if they are no longer needed after UI changes
// import com.example.smarthomeappfinal.ui.home.HomeViewModel
// import com.example.smarthomeappfinal.ui.dashboard.DashboardViewModel
// import com.example.smarthomeappfinal.ui.notifications.NotificationsViewModel

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var actionBarSpinner: Spinner? = null
    private lateinit var navController: NavController
    private lateinit var navigationManager: NavigationManager
    override lateinit var preferencesManager: PreferencesManager
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var appBarConfiguration: AppBarConfiguration
    
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
        savedInstanceState?.getInt(KEY_SPINNER_POSITION)
        setupComponents()
    }

    private fun setupComponents() {
        setupToolbarAndNavigationClickListener()
        setupDrawer()
        setupNavigation()
        setupSpinner()
        setupBottomNavigation()
    }

    private fun setupToolbarAndNavigationClickListener() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener { 
            Log.d("DEBUG_NAV", "Toolbar Navigation Icon Clicked!")
            val currentDestId = navController.currentDestination?.id
            if (currentDestId == R.id.navigation_camera_capture) {
                if (binding.drawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
                    if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.openDrawer(GravityCompat.START)
                        Log.d("DEBUG_NAV", "Toolbar click: Opened drawer on camera page")
                    } else {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        Log.d("DEBUG_NAV", "Toolbar click: Closed drawer on camera page")
                    }
                } else {
                    Log.d("DEBUG_NAV", "Toolbar click: Drawer locked on camera page.")
                }
            } else {
                Log.d("DEBUG_NAV", "Toolbar click: Not camera page, calling onSupportNavigateUp()")
                onSupportNavigateUp() 
            }
        }
    }

    private fun setupDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = false
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.cameraDrawer.setNavigationItemSelectedListener(this)
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
                R.id.navigation_notifications,
                R.id.navigation_camera_capture 
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupSpinner() {
        actionBarSpinner = Spinner(this).apply { id = View.generateViewId() }
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.viewer_options_array, R.layout.action_bar_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        actionBarSpinner?.adapter = adapter
        val position = savedSpinnerPosition ?: preferencesManager.getLastSelectedMode().value
        actionBarSpinner?.setSelection(position, false)
        actionBarSpinner?.onItemSelectedListener = createSpinnerListener()
        setupSpinnerInActionBar()
        isSpinnerInitialized = true
    }

    private fun createSpinnerListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (!isSpinnerInitialized) return
            val mode = AppMode.fromInt(position)
            preferencesManager.saveAppMode(mode)
            navigationManager.navigateBasedOnMode(navController, mode)
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun setupSpinnerInActionBar() {
        val params = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.START)
        supportActionBar?.apply {
            setCustomView(actionBarSpinner, params)
            setDisplayShowCustomEnabled(true)
        }
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
            // Default states for most screens
            binding.toolbar.visibility = View.VISIBLE
            binding.navView.visibility = View.VISIBLE
            binding.cameraDrawer.visibility = View.GONE
            binding.navigationView.visibility = View.VISIBLE // Assuming navigationView is the main drawer content
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle.isDrawerIndicatorEnabled = appBarConfiguration.topLevelDestinations.contains(destination.id) // Burger for top-level, back for others

            supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show icon (burger or back)

            // Specific adjustments for camera screen
            if (destination.id == R.id.navigation_camera_capture) {
                binding.navView.visibility = View.GONE
                binding.cameraDrawer.visibility = View.VISIBLE
                binding.navigationView.visibility = View.GONE // Hide main drawer content
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // Unlock for camera
                drawerToggle.isDrawerIndicatorEnabled = true // Ensure burger icon for camera drawer
                // Optionally, set a specific burger icon if needed:
                // supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) 
            } else {
                // Ensure the correct drawer content (main) is visible if not camera
                binding.navigationView.visibility = View.VISIBLE
                binding.cameraDrawer.visibility = View.GONE
                // Standard behavior for other screens (burger if top-level, else back arrow)
                // This is mostly handled by setupActionBarWithNavController and appBarConfiguration
                // but isDrawerIndicatorEnabled helps reinforce it for the toggle.
                if (appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // Allow drawer for top-level destinations
                    drawerToggle.isDrawerIndicatorEnabled = true
                } else {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    drawerToggle.isDrawerIndicatorEnabled = false
                }
            }

            // Adjust toolbar home indicator based on whether it's a drawer or up action
            if (drawerToggle.isDrawerIndicatorEnabled) {
                 supportActionBar?.setHomeAsUpIndicator(null) // Use drawer toggle's icon
            } else {
                // For non-drawer (up) actions, NavController usually sets this,
                // but we can ensure it's not the drawer toggle's icon.
                // Or, rely on setupActionBarWithNavController to set the up arrow.
            }

            // Handle visibility of spinner
            val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
            if (currentFragment is CameraCaptureFragment || currentFragment is MonitorFragment) {
                supportActionBar?.setDisplayShowCustomEnabled(false)
                actionBarSpinner?.visibility = View.GONE
                Log.d("SPINNER_VIS", "Spinner hidden for Camera/Monitor Fragment")
            } else {
                supportActionBar?.setDisplayShowCustomEnabled(true)
                actionBarSpinner?.visibility = View.VISIBLE
                Log.d("SPINNER_VIS", "Spinner shown for other fragments")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("DEBUG_NAV", "onOptionsItemSelected: ${item.itemId}, android.R.id.home: ${android.R.id.home}")
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentDestId = navController.currentDestination?.id
        // If on camera page, and drawer is open, close it. Otherwise, let NavController handle "Up".
        if (currentDestId == R.id.navigation_camera_capture && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("NAV_ITEM_SELECTED", "Item selected: ${item.title}")
        var handled = false
        val currentDestId = navController.currentDestination?.id

        if (currentDestId == R.id.navigation_camera_capture) {
            // Handle camera_drawer items
            when (item.itemId) {
                R.id.nav_camera_settings -> {
                    showToast("Camera Settings clicked")
                    handled = true
                }
                R.id.nav_manage_recordings -> {
                    showToast("Manage Recordings clicked")
                    handled = true
                }
                // Add other camera_drawer items here
            }
        } else {
            // Handle main navigationView items (existing logic or new)
            when (item.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.navigation_settings)
                    handled = true
                }
                R.id.nav_logout -> {
                    // Implement logout
                    showToast("Logout clicked")
                    handled = true
                }
                 R.id.nav_home -> {
                    navController.navigate(R.id.navigation_home)
                    handled = true
                }
                R.id.nav_dashboard -> { // Example if you have dashboard in drawer
                    navController.navigate(R.id.navigation_dashboard)
                    handled = true
                }
                R.id.nav_notifications_drawer -> { // Assuming you have a notifications item in the drawer
                    navController.navigate(R.id.navigation_notifications)
                    handled = true
                }
            }
        }

        if (handled) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Fallback for items not explicitly handled, or let default behavior if part of NavController setup
            // For example, if items are directly tied to navigation destinations via menu XML and NavController
            // handled = NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
            showToast("Unhandled navigation item: ${item.title}")
        }
        return handled // Return true if the item selection was handled
    }

    // To ensure the drawer toggle syncs its state (e.g., burger or back arrow)
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    // Make sure to remove or adjust the custom toolbar navigation click listener
    // if drawerToggle.isDrawerIndicatorEnabled = true is correctly managed by onDestinationChangedListener
    // The setupToolbarAndNavigationClickListener might need simplification or removal
    // if the ActionBarDrawerToggle handles all cases correctly with the updated logic.
    // For now, I'll leave setupToolbarAndNavigationClickListener as is, as the camera page
    // has specific logic within it that might still be needed if not fully covered by drawerToggle.

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        actionBarSpinner?.selectedItemPosition?.let { position ->
            outState.putInt(KEY_SPINNER_POSITION, position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        actionBarSpinner = null
        _binding = null
    }

    companion object {
        private const val KEY_SPINNER_POSITION = "spinner_position"
    }
}