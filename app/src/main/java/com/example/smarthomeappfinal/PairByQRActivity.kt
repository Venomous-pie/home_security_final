package com.example.smarthomeappfinal

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class PairByQRActivity : AppCompatActivity() {

    private lateinit var ivQRCode: ImageView
    private lateinit var tvExpirationTimer: TextView
    private lateinit var ivClose: ImageView
    private lateinit var tvHelp: TextView

    private var countDownTimer: CountDownTimer? = null
    private val qrCodeValidityMillis: Long = 30 * 60 * 1000 // 30 minutes

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_by_qr)

        ivQRCode = findViewById(R.id.ivQRCode)
        tvExpirationTimer = findViewById(R.id.tvExpirationTimer)
        ivClose = findViewById(R.id.ivClosePairByQR)
        tvHelp = findViewById(R.id.tvHelpPairByQR)

        val userId = intent.getStringExtra(EXTRA_USER_ID)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: User ID not found for QR generation.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        generateAndDisplayQRCode(userId)
        startExpirationTimer()

        ivClose.setOnClickListener {
            finish()
        }

        tvHelp.setOnClickListener {
            // Placeholder for Help action
            Toast.makeText(this, "Help clicked (Placeholder)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAndDisplayQRCode(data: String) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 600, 600) // Adjust size as needed
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            ivQRCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startExpirationTimer() {
        countDownTimer?.cancel() // Cancel any existing timer
        countDownTimer = object : CountDownTimer(qrCodeValidityMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                
                val timerText = String.format("This code will expire in %d min %02d sec.", minutes, seconds)
                val spannable = SpannableString(timerText)

                // Color for minutes
                val minStartIndex = timerText.indexOf(minutes.toString())
                val minEndIndex = minStartIndex + minutes.toString().length
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FFA500")), minStartIndex, minEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Color for seconds 
                val secStartIndex = timerText.indexOf(String.format("%02d", seconds))
                val secEndIndex = secStartIndex + String.format("%02d", seconds).length
                 spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FFA500")), secStartIndex, secEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                tvExpirationTimer.text = spannable
            }

            override fun onFinish() {
                tvExpirationTimer.text = "QR code expired. Please generate a new one."
                ivQRCode.setImageDrawable(null) // Clear QR code or show an expired image
                ivQRCode.setBackgroundColor(Color.LTGRAY)
                // Optionally, disable help/close or provide a way to regenerate
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
} 