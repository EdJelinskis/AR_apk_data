package com.example.agrorob

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {
    private val splashTimeForAct: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashLogo: ImageView = findViewById(R.id.splashLogo)
        splashLogo.alpha = 0f
        splashLogo.animate().setDuration(1500).alpha(1f).withEndAction{
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            finish()
        }
    }
}