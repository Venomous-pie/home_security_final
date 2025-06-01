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
import com.example.smarthomeappfinal.base.BaseFragment
import com.example.smarthomeappfinal.databinding.FragmentMonitorBinding
import com.example.smarthomeappfinal.ui.devices.DeviceAdapter
import com.example.smarthomeappfinal.ui.devices.DeviceListViewModel
import kotlinx.coroutines.launch

class MonitorFragment : BaseFragment() {
    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeviceListViewModel by viewModels()
    private var deviceAdapter: DeviceAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefreshLayout()
        setupAddCameraButton()
        observeViewModel()
        viewModel.loadDevices()
    }

    private fun setupRecyclerView() {
        try {
            deviceAdapter = DeviceAdapter { device, action ->
                when (action) {
                    "view" -> {
                        findNavController().navigate(
                            MonitorFragmentDirections.actionMonitorToStreamView(device.id)
                        )
                    }
                    // Add other actions if needed
                }
            }

            binding.recyclerViewDevices?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = deviceAdapter
                isNestedScrollingEnabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.loadDevices()
        }
    }

    private fun setupAddCameraButton() {
        binding.btnAddCamera?.setOnClickListener {
            // Navigate to camera setup activity
            val intent = Intent(requireContext(), PrepareDeviceActivity::class.java).apply {
                putExtra("device_type", "camera")
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.devices.collect { devices ->
                        val monitorDevices = devices.filter { it.type.lowercase() == "monitor" }
                        if (monitorDevices.isEmpty()) {
                            showEmptyState()
                        } else {
                            showDeviceList()
                            deviceAdapter?.submitList(monitorDevices)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.error.collect { error ->
                        error?.let {
                            showError(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.refreshing.collect { isRefreshing ->
                        binding.swipeRefreshLayout?.isRefreshing = isRefreshing
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.apply {
            recyclerViewDevices?.visibility = View.GONE
            emptyStateGroup?.visibility = View.VISIBLE
        }
    }

    private fun showDeviceList() {
        binding.apply {
            recyclerViewDevices?.visibility = View.VISIBLE
            emptyStateGroup?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deviceAdapter = null
        _binding = null
    }
}