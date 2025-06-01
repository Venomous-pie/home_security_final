package com.example.smarthomeappfinal.ui.home

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
import com.example.smarthomeappfinal.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var activityAdapter: ActivityAdapter? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.loadData()
    }

    private fun setupRecyclerView() {
        try {
            activityAdapter = ActivityAdapter()
            binding.rvRecentActivity?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = activityAdapter
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
                    viewModel.recentActivities.collectLatest { activities ->
                        try {
                            activityAdapter?.submitList(activities)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    viewModel.cameraCount.collectLatest { count ->
                        try {
                            binding.tvCameraCount?.text = count.toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                launch {
                    viewModel.viewerCount.collectLatest { count ->
                        try {
                            binding.tvViewerCount?.text = count.toString()
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
        activityAdapter = null
        _binding = null
    }
}