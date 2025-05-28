package com.example.smarthomeappfinal.ui.camera

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthomeappfinal.MainActivity
import com.example.smarthomeappfinal.R
import com.google.android.material.snackbar.Snackbar // Or your preferred way to show messages

class CameraCaptureFragment : Fragment() {

    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_capture, container, false)
        mainActivity = activity as? MainActivity
        setHasOptionsMenu(true) // Retain if you have other options menu items

        return view
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.let { act ->
            act.configureAppBarForSpinner(false)
            act.configureAppBarForTitle(true, "Camera")

            val toolbar = view?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_camera_capture)
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

            // No drawer toggle, but still show home as up if needed for other navigation
            act.supportActionBar?.setDisplayHomeAsUpEnabled(true) 
            act.supportActionBar?.setHomeButtonEnabled(true)
        }
        mainActivity?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        mainActivity?.let { act ->
            act.configureAppBarForTitle(false, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity = null
    }

    // Remove onOptionsItemSelected if only used for toggle
    // override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //     // Handle other menu items if any
    //     return super.onOptionsItemSelected(item)
    // }

    // Remove onNavigationItemSelected as NavigationView is removed

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }
} 