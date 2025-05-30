package com.example.smarthomeappfinal

import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smarthomeappfinal.auth.AuthState
import com.example.smarthomeappfinal.auth.AuthViewModel
import com.example.smarthomeappfinal.webrtc.PairingData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class PairByQRActivity : AppCompatActivity() {

    private lateinit var ivQRCode: ImageView
    private lateinit var tvExpirationTimer: TextView
    private lateinit var ivClose: ImageView
    private lateinit var tvHelp: TextView
    private lateinit var progressBar: ProgressBar

    private var countDownTimer: CountDownTimer? = null
    private val qrCodeValidityMillis: Long = 30 * 60 * 1000 // 30 minutes

    private val viewModel: AuthViewModel by viewModels()

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        private const val SIGNALING_SERVER_URL = "wss://your-signaling-server.com" // Replace with your server URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_by_qr)

        setupViews()
        setupClickListeners()
        observeAuthState()
        
        val userId = intent.getStringExtra(EXTRA_USER_ID)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (userId.isNullOrEmpty() || currentUser == null) {
            showError("Error: User ID not found for QR generation.")
            finish()
            return
        }

        generateAndDisplayQRCode(userId, currentUser.getIdToken(false).result?.token)
        startExpirationTimer()
    }

    private fun setupViews() {
        ivQRCode = findViewById(R.id.ivQRCode)
        tvExpirationTimer = findViewById(R.id.tvExpirationTimer)
        ivClose = findViewById(R.id.ivClosePairByQR)
        tvHelp = findViewById(R.id.tvHelpPairByQR)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        ivClose.setOnClickListener { finish() }
        tvHelp.setOnClickListener { showHelpDialog() }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthState.Idle -> {
                            progressBar.visibility = View.GONE
                        }
                        is AuthState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        is AuthState.Success -> {
                            progressBar.visibility = View.GONE
                            // Handle successful authentication
                            state.user?.let {
                                // Navigate to next screen or update UI
                            }
                        }
                        is AuthState.Error -> {
                            progressBar.visibility = View.GONE
                            showError(state.throwable.message ?: "Authentication failed")
                        }
                    }
                }
            }
        }
    }

    private fun generateAndDisplayQRCode(userId: String, idToken: String?) {
        if (idToken == null) {
            showError("Error: Unable to get security token")
            finish()
            return
        }

        val pairingData = PairingData(
            userId = userId,
            deviceId = UUID.randomUUID().toString(),
            securityToken = idToken,
            serverUrl = SIGNALING_SERVER_URL
        )

        val qrBitmap = pairingData.generateQrCode()
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap)
        } else {
            showError("Error generating QR code")
        }
    }

    private fun startExpirationTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(qrCodeValidityMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                
                val timerText = String.format("This code will expire in %d min %02d sec.", minutes, seconds)
                val spannable = SpannableString(timerText)

                // Color for minutes
                val minStartIndex = timerText.indexOf(minutes.toString())
                val minEndIndex = minStartIndex + minutes.toString().length
                spannable.setSpan(
                    ForegroundColorSpan(getColor(R.color.warning_orange)),
                    minStartIndex,
                    minEndIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Color for seconds
                val secStartIndex = timerText.indexOf(String.format("%02d", seconds))
                val secEndIndex = secStartIndex + String.format("%02d", seconds).length
                spannable.setSpan(
                    ForegroundColorSpan(getColor(R.color.warning_orange)),
                    secStartIndex,
                    secEndIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                tvExpirationTimer.text = spannable
            }

            override fun onFinish() {
                tvExpirationTimer.text = getString(R.string.qr_code_expired)
                ivQRCode.setImageDrawable(null)
                ivQRCode.setBackgroundColor(getColor(R.color.disabled_gray))
            }
        }.start()
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showHelpDialog() {
        // Show help dialog with pairing instructions
        Toast.makeText(this, "Help clicked (Placeholder)", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
} 