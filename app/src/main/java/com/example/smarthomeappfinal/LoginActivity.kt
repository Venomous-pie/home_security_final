package com.example.smarthomeappfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLoginSignUp: Button = findViewById(R.id.btnLoginSignUp)
        val btnLoginGuest: Button = findViewById(R.id.btnLoginGuest)

        btnLoginSignUp.setOnClickListener {
            // Navigate to EmailLoginActivity
            startActivity(Intent(this, EmailLoginActivity::class.java))
            // We don't finish this activity, so user can go back to choose guest option
        }

        btnLoginGuest.setOnClickListener {
            Toast.makeText(this, "Continue as Guest clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finish LoginActivity so user can't go back to it with back button
        }
    }
} 