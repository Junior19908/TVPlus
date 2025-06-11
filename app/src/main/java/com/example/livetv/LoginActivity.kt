package com.example.livetv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class LoginActivity : AppCompatActivity() {

    private val validCode = "tvplus123" // código fixo para exemplo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val input = findViewById<EditText>(R.id.code_input)
        val button = findViewById<Button>(R.id.login_button)
        val error = findViewById<TextView>(R.id.error_text)

        button.setOnClickListener {
            val code = input.text.toString().trim()

            if (code == validCode) {
                // Salva timestamp
                val prefs = getSharedPreferences("TVPlusPrefs", MODE_PRIVATE)
                prefs.edit().putLong("login_time", System.currentTimeMillis()).apply()

                // Vai pra Home
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                error.visibility = View.VISIBLE
            }
        }
    }
}
