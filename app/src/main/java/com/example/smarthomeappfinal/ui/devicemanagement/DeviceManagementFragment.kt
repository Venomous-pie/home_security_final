package com.example.smarthomeappfinal.ui.devicemanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentDeviceManagementBinding

class DeviceManagementFragment : Fragment() {

    private var _binding: FragmentDeviceManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabScanQr.setOnClickListener {
            findNavController().navigate(R.id.action_device_management_to_scan_qr)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 