package com.skysinc.tvplus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.skysinc.tvplus.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoginValid()) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }

    private fun isLoginValid(): Boolean {
        val prefs = getSharedPreferences("TVPlusPrefs", MODE_PRIVATE)
        val loginTime = prefs.getLong("login_time", 0L)
        val currentTime = System.currentTimeMillis()
        val hours48 = 48 * 60 * 60 * 1000 // 48 horas em millis
        return (currentTime - loginTime) < hours48
    }
}
