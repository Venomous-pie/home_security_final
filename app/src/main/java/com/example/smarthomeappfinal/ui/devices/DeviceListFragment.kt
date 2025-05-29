package com.example.smarthomeappfinal.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthomeappfinal.base.BaseFragment
import com.example.smarthomeappfinal.databinding.FragmentDeviceListBinding
import com.example.smarthomeappfinal.network.DeviceResponse
import com.google.android.material.divider.MaterialDividerItemDecoration

class DeviceListFragment : BaseFragment() {
    private var _binding: FragmentDeviceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device, command ->
            viewModel.controlDevice(device.id, command)
        }

        with(binding.recyclerViewDevices) {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                MaterialDividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadDevices()
        }
    }

    private fun observeViewModel() {
        launchWhenStarted {
            viewModel.devices.collect { devices ->
                updateDevices(devices)
            }
        }

        launchWhenStarted {
            viewModel.refreshing.collect { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }

        launchWhenStarted {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        launchWhenStarted {
            viewModel.error.collect { error ->
                showError(error)
            }
        }
    }

    private fun updateDevices(devices: List<DeviceResponse>) {
        if (devices.isEmpty()) {
            binding.textViewNoDevices.visibility = View.VISIBLE
            binding.recyclerViewDevices.visibility = View.GONE
        } else {
            binding.textViewNoDevices.visibility = View.GONE
            binding.recyclerViewDevices.visibility = View.VISIBLE
            deviceAdapter.submitList(devices)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 