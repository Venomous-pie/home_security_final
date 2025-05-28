package com.example.smarthomeappfinal.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
// import android.widget.Toast // No longer needed for placeholder
import androidx.fragment.app.Fragment
import com.example.smarthomeappfinal.PrepareDeviceActivity
import com.example.smarthomeappfinal.R
// import com.example.smarthomeappfinal.databinding.FragmentHomeBinding // Binding might not be necessary if direct view access is simple
// import androidx.lifecycle.ViewModelProvider // ViewModel might not be needed for this static-like page

class HomeFragment : Fragment() {

    // private var _binding: FragmentHomeBinding? = null
    // private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Example: Set up the "Add Camera" button click listener
        val btnAddCamera: Button? = view.findViewById(R.id.btnAddCamera)
        btnAddCamera?.setOnClickListener {
            val intent = Intent(activity, PrepareDeviceActivity::class.java)
            startActivity(intent)
        }
        
        return view
    }

    // override fun onDestroyView() {
    //     super.onDestroyView()
    //     _binding = null
    // }
}