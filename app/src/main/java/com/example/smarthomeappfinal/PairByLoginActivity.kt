package com.example.smarthomeappfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PairByLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_by_login)

        auth = Firebase.auth

        val ivBackArrow: ImageView = findViewById(R.id.ivBackArrowPairByLogin)
        val btnPairByQRCode: Button = findViewById(R.id.btnPairByQRCode)

        ivBackArrow.setOnClickListener {
            finish() // Go back to the previous screen (PrepareDeviceActivity)
        }

        btnPairByQRCode.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val intent = Intent(this, PairByQRActivity::class.java).apply {
                    putExtra(PairByQRActivity.EXTRA_USER_ID, userId)
                }
                startActivity(intent)
            } else {
                // This case should ideally not happen if user reached this screen after login flow
                Toast.makeText(this, "User not logged in. Cannot generate QR code.", Toast.LENGTH_LONG).show()
            }
        }
        
        // You might also want to add logic that after a certain time, if no pairing confirmation is received,
        // this screen automatically times out or offers other options.
    }
} 