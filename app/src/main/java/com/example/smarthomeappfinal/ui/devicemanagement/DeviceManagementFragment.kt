package com.example.smarthomeappfinal.ui.devicemanagement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthomeappfinal.PrepareDeviceActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.base.BaseFragment
import com.example.smarthomeappfinal.databinding.FragmentDeviceManagementBinding
import com.example.smarthomeappfinal.ui.devices.DeviceAdapter
import com.google.android.material.snackbar.Snackbar

class DeviceManagementFragment : BaseFragment() {

    private var _binding: FragmentDeviceManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeviceManagementViewModel by viewModels()
    
    private lateinit var cameraAdapter: DeviceAdapter
    private lateinit var viewerAdapter: DeviceAdapter

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
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        cameraAdapter = DeviceAdapter { device, action ->
            when (action) {
                "show_details" -> showDeviceDetails(device.id)
                else -> viewModel.controlDevice(device.id, action)
            }
        }

        viewerAdapter = DeviceAdapter { device, action ->
            when (action) {
                "show_details" -> showDeviceDetails(device.id)
                else -> viewModel.controlDevice(device.id, action)
            }
        }

        binding.rvCameraDevices.apply {
            adapter = cameraAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }

        binding.rvViewerDevices.apply {
            adapter = viewerAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnAddCamera.setOnClickListener {
            startActivity(Intent(requireContext(), PrepareDeviceActivity::class.java))
        }

        binding.btnPairViewer.setOnClickListener {
            findNavController().navigate(R.id.action_device_management_to_scan_qr)
        }

        binding.btnScanQr.setOnClickListener {
            findNavController().navigate(R.id.action_device_management_to_scan_qr)
        }
    }

    private fun observeViewModel() {
        viewModel.cameraDevices.observe(viewLifecycleOwner) { devices ->
            cameraAdapter.submitList(devices)
        }

        viewModel.viewerDevices.observe(viewLifecycleOwner) { devices ->
            viewerAdapter.submitList(devices)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeviceDetails(deviceId: String) {
        // TODO: Navigate to device details screen
        Snackbar.make(binding.root, "Showing details for device $deviceId", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}