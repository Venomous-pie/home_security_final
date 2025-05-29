package com.example.smarthomeappfinal.ui.camera

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.smarthomeappfinal.MainActivity
import com.example.smarthomeappfinal.R
import com.google.android.material.snackbar.Snackbar

class CameraCaptureFragment : Fragment() {

    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_capture, container, false)
        mainActivity = activity as? MainActivity
        return view
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity = null
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }
} 