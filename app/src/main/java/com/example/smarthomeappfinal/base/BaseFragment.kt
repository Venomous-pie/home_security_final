package com.example.smarthomeappfinal.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smarthomeappfinal.MainActivity
import com.example.smarthomeappfinal.navigation.NavigationManager
import com.example.smarthomeappfinal.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment() {
    protected lateinit var navigationManager: NavigationManager
    protected lateinit var preferencesManager: PreferencesManager
    protected var mainActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationManager = NavigationManager.getInstance(requireContext())
        preferencesManager = PreferencesManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as? MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity = null
    }

    protected fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    protected fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                block()
            } catch (e: Exception) {
                showError(e.message ?: "An error occurred")
            }
        }
    }

    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    block()
                } catch (e: Exception) {
                    showError(e.message ?: "An error occurred")
                }
            }
        }
    }
} 