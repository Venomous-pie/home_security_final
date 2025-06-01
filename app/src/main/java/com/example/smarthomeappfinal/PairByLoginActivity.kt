package com.example.smarthomeappfinal

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smarthomeappfinal.databinding.ActivityPairByLoginBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PairByLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPairByLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairByLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceType = intent.getStringExtra("device_type") ?: "camera"
        val setupMode = intent.getStringExtra("setup_mode") ?: "wifi"

        setupViews(deviceType, setupMode)
        setupListeners()
    }

    private fun setupViews(deviceType: String, setupMode: String) {
        binding.apply {
            tvTitle.text = when (setupMode) {
                "wifi" -> "Connect WiFi Camera"
                else -> "Connect Mobile Device"
            }

            // Show appropriate setup instructions
            tvInstructions.text = when (setupMode) {
                "wifi" -> "Enter the credentials printed on your camera or scan the QR code"
                else -> "Install the app on your other device and enter the pairing code shown"
            }

            // Show/hide relevant input fields
            if (setupMode == "wifi") {
                layoutWifiCredentials.visibility = View.VISIBLE
                layoutPairingCode.visibility = View.GONE
                btnScanQr.visibility = View.VISIBLE
            } else {
                layoutWifiCredentials.visibility = View.GONE
                layoutPairingCode.visibility = View.VISIBLE
                btnScanQr.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                finish()
            }

            btnScanQr.setOnClickListener {
                // Launch QR scanner
                Toast.makeText(this@PairByLoginActivity, "QR scanning coming soon", Toast.LENGTH_SHORT).show()
            }

            btnConnect.setOnClickListener {
                if (validateInput()) {
                    startPairing()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        binding.apply {
            val setupMode = intent.getStringExtra("setup_mode") ?: "wifi"
            
            if (setupMode == "wifi") {
                if (etDeviceId.text.isNullOrEmpty()) {
                    etDeviceId.error = "Device ID is required"
                    return false
                }
                if (etPassword.text.isNullOrEmpty()) {
                    etPassword.error = "Password is required"
                    return false
                }
            } else {
                if (etPairingCode.text.isNullOrEmpty()) {
                    etPairingCode.error = "Pairing code is required"
                    return false
                }
            }
        }
        return true
    }

    private fun startPairing() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            btnConnect.isEnabled = false
            btnScanQr.isEnabled = false

            lifecycleScope.launch {
                try {
                    val setupMode = intent.getStringExtra("setup_mode") ?: "wifi"
                    
                    // Simulate network request
                    delay(2000)

                    when (setupMode) {
                        "wifi" -> {
                            // TODO: Implement WiFi camera pairing
                            val deviceId = etDeviceId.text.toString()
                            val password = etPassword.text.toString()
                            // Connect to camera using credentials
                        }
                        else -> {
                            // TODO: Implement mobile device pairing
                            val pairingCode = etPairingCode.text.toString()
                            // Pair with mobile device using code
                        }
                    }

                    Toast.makeText(
                        this@PairByLoginActivity,
                        "Device connected successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Return to monitors screen
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@PairByLoginActivity,
                        "Connection failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    progressBar.visibility = View.GONE
                    btnConnect.isEnabled = true
                    btnScanQr.isEnabled = true
                }
            }
        }
    }
}