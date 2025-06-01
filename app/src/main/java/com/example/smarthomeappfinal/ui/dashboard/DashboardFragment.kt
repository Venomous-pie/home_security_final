package com.example.smarthomeappfinal.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthomeappfinal.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private var deviceStatsAdapter: DeviceStatAdapter? = null
    private var batteryStatusAdapter: BatteryStatusAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setupRecyclerViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        // Trigger data loading
        viewModel.loadData()
    }

    private fun setupRecyclerViews() {
        try {
            deviceStatsAdapter = DeviceStatAdapter()
            batteryStatusAdapter = BatteryStatusAdapter()

            binding.rvDeviceStats?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = deviceStatsAdapter
                isNestedScrollingEnabled = false
            }

            binding.rvBatteryStatus?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = batteryStatusAdapter
                isNestedScrollingEnabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.systemHealth.collect { health ->
                        try {
                            binding.apply {
                                storageProgress?.progress = health.storageUsagePercent
                                tvStorageInfo?.text = "${formatSize(health.storageUsed)} used of ${formatSize(health.storageTotal)}"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    viewModel.networkStats.collect { stats ->
                        try {
                            binding.apply {
                                tvUploadSpeed?.text = formatSpeed(stats.uploadSpeed)
                                tvDownloadSpeed?.text = formatSpeed(stats.downloadSpeed)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    viewModel.deviceStats.collect { stats ->
                        try {
                            deviceStatsAdapter?.submitList(stats)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    viewModel.batteryStatus.collect { status ->
                        try {
                            batteryStatusAdapter?.submitList(status)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deviceStatsAdapter = null
        batteryStatusAdapter = null
        _binding = null
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "%.1f %s".format(size, units[unitIndex])
    }

    private fun formatSpeed(bitsPerSecond: Long): String {
        val mbps = bitsPerSecond.toDouble() / (1024 * 1024)
        return "%.1f Mbps".format(mbps)
    }
}