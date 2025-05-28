package com.example.smarthomeappfinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class PrepareDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_device)

        val ivBackArrow: ImageView = findViewById(R.id.ivBackArrowPrepareDevice)
        val cardMobileDevice: MaterialCardView = findViewById(R.id.cardMobileDevice)
        val tvSetUpLater: TextView = findViewById(R.id.tvSetUpLater)

        ivBackArrow.setOnClickListener {
            finish() // Go back to the previous screen
        }

        cardMobileDevice.setOnClickListener {
            // Navigate to PairByLoginActivity
            val intent = Intent(this, PairByLoginActivity::class.java)
            startActivity(intent)
        }

        tvSetUpLater.setOnClickListener {
            // Handle "Set up later" - e.g., navigate back to the main monitor screen or close this flow
            Toast.makeText(this, "Set up later clicked", Toast.LENGTH_SHORT).show()
            finish() // For now, just finish the activity
        }
    }
} 