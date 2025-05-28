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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "EmailLoginActivity"

    // Declare EditTexts as class-level properties
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        auth = Firebase.auth

        // Initialize class-level properties
        etEmail = findViewById(R.id.etEmail) 
        etPassword = findViewById(R.id.etPassword) 

        val ivBackArrow: ImageView = findViewById(R.id.ivBackArrow)
        val btnContinueWithEmail: Button = findViewById(R.id.btnContinueWithEmail)
        val tvRegisterLink: TextView = findViewById(R.id.tvRegisterLink)
        val btnScanToLink: Button = findViewById(R.id.btnScanToLink)

        // For the underlined text in register link
        tvRegisterLink.text = Html.fromHtml(getString(R.string.register_link_text), Html.FROM_HTML_MODE_LEGACY)
        tvRegisterLink.movementMethod = LinkMovementMethod.getInstance() // Make the link clickable

        ivBackArrow.setOnClickListener {
            finish()
        }

        btnContinueWithEmail.setOnClickListener { // This button is now for LOGIN only
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                        updateUI(null)
                    }
                }
        }

        tvRegisterLink.setOnClickListener {
            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnScanToLink.setOnClickListener {
            Toast.makeText(this, "Scan to Link clicked (Placeholder)", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onStart() {
        super.onStart()
        // val currentUser = auth.currentUser
        // if(currentUser != null){
        //    updateUI(currentUser)
        // }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            Toast.makeText(this, "Logged in as ${currentUser.email}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Log.d(TAG, "User is signed out or action failed")
        }
    }
} 