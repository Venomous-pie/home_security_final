package com.example.smarthomeappfinal.ui.monitor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthomeappfinal.PrepareDeviceActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.base.BaseFragment
import com.example.smarthomeappfinal.databinding.FragmentMonitorBinding
import com.example.smarthomeappfinal.ui.devices.DeviceAdapter
import com.example.smarthomeappfinal.ui.devices.DeviceListViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MonitorFragment : BaseFragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device, command ->
            when (command) {
                "view_stream" -> {
                    val action = MonitorFragmentDirections.actionMonitorToStreamView(device.id)
                    findNavController().navigate(action)
                }
                else -> {
                    viewModel.controlDevice(device.id, command)
                }
            }
        }

        binding.recyclerViewDevices.apply {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(context)
            visibility = View.GONE // Initially hidden until we have data
        }
    }

    private fun setupClickListeners() {
        binding.btnAddCamera.setOnClickListener {
            val intent = Intent(activity, PrepareDeviceActivity::class.java)
            startActivity(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadDevices()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.devices.collect { devices ->
                    val monitorDevices = devices.filter { it.type.lowercase() == "monitor" }
                    if (monitorDevices.isEmpty()) {
                        showEmptyState()
                    } else {
                        showDeviceList()
                        deviceAdapter.submitList(monitorDevices)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshing.collect { isRefreshing ->
                    binding.swipeRefreshLayout.isRefreshing = isRefreshing
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateGroup.visibility = View.VISIBLE
        binding.recyclerViewDevices.visibility = View.GONE
    }

    private fun showDeviceList() {
        binding.emptyStateGroup.visibility = View.GONE
        binding.recyclerViewDevices.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 