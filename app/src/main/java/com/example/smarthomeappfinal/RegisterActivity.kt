package com.example.smarthomeappfinal

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "RegisterActivity"

    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var ivBackArrowRegister: ImageView
    private lateinit var tvLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        ivBackArrowRegister = findViewById(R.id.ivBackArrowRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        // Make the login link clickable
        tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
        // Set an OnClickListener for the login link
        tvLoginLink.setOnClickListener {
            // Navigate to EmailLoginActivity
            val intent = Intent(this, EmailLoginActivity::class.java)
            // Clear the task stack and start a new one for EmailLoginActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish RegisterActivity
        }

        ivBackArrowRegister.setOnClickListener {
            finish() // Simply finish this activity to go back
        }

        btnRegister.setOnClickListener {
            val email = etRegisterEmail.text.toString().trim()
            val password = etRegisterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        Toast.makeText(baseContext, "Account created successfully! Please log in.",
                            Toast.LENGTH_LONG).show()
                        // Navigate to LoginActivity after successful registration
                        val intent = Intent(this, EmailLoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Account creation failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        // You can add logic here if needed, for example, to navigate away if a user is already logged in.
        if (currentUser != null) {
            // Already logged in, perhaps navigate to MainActivity
        } else {
            // Stay on Register screen or handle error
        }
    }
} 