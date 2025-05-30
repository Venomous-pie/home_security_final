package com.example.smarthomeappfinal.ui.monitor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.smarthomeappfinal.PrepareDeviceActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentMonitorBinding

class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        
        // Set up the "Add Camera" button click listener
        binding.btnAddCamera.setOnClickListener {
            val intent = Intent(activity, PrepareDeviceActivity::class.java)
            startActivity(intent)
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // TODO: Implement monitor UI setup
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 