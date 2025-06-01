package com.example.smarthomeappfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class PrepareDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_device)

        val deviceType = intent.getStringExtra("device_type") ?: "camera"
        setupViews(deviceType)
    }

    private fun setupViews(deviceType: String) {
        val ivBackArrow: ImageView = findViewById(R.id.ivBackArrowPrepareDevice)
        val cardMobileDevice: MaterialCardView = findViewById(R.id.cardMobileDevice)
        val cardWifiCamera: MaterialCardView = findViewById(R.id.cardWifiCamera)
        val tvSetUpLater: TextView = findViewById(R.id.tvSetUpLater)
        val tvTitle: TextView = findViewById(R.id.tvTitlePrepareDevice)

        // Update title based on device type
        when (deviceType) {
            "camera" -> {
                tvTitle.text = "Add a Camera"
                cardWifiCamera.visibility = View.VISIBLE
                cardMobileDevice.visibility = View.VISIBLE
            }
            else -> {
                tvTitle.text = "Add a Device"
                cardWifiCamera.visibility = View.GONE
                cardMobileDevice.visibility = View.VISIBLE
            }
        }

        ivBackArrow.setOnClickListener {
            finish()
        }

        cardMobileDevice.setOnClickListener {
            val intent = Intent(this, PairByLoginActivity::class.java).apply {
                putExtra("device_type", deviceType)
                putExtra("setup_mode", "mobile")
            }
            startActivity(intent)
        }

        cardWifiCamera.setOnClickListener {
            val intent = Intent(this, PairByLoginActivity::class.java).apply {
                putExtra("device_type", deviceType)
                putExtra("setup_mode", "wifi")
            }
            startActivity(intent)
        }

        tvSetUpLater.setOnClickListener {
            Toast.makeText(this, "You can add a camera later from the Monitors screen", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}