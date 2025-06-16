package com.skysinc.tvplus

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.skysinc.tvplus.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class LoginActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val input = findViewById<EditText>(R.id.code_input)
        val button = findViewById<Button>(R.id.login_button)
        val error = findViewById<TextView>(R.id.error_text)

        button.setOnClickListener {
            val code = input.text.toString().trim().uppercase()
            validarChaveNoFirebase(code, error)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validarChaveNoFirebase(codigo: String, errorText: TextView) {
        val db = Firebase.firestore

        db.collection("licencas")
            .whereEqualTo("chave", codigo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0] // Pega o primeiro resultado

                    val ativo = doc.getBoolean("ativo") ?: false
                    val dataExp = doc.getTimestamp("dataExpiracao")?.toDate()

                    if (!ativo) {
                        errorText.text = "Licença desativada."
                        errorText.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    if (dataExp != null && dataExp.after(Date())) {
                        val prefs = getSharedPreferences("TVPlusPrefs", MODE_PRIVATE)
                        prefs.edit().putLong("login_time", System.currentTimeMillis()).apply()

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        errorText.text = "Licença expirada."
                        errorText.visibility = View.VISIBLE
                    }

                } else {
                    errorText.text = "Chave não encontrada."
                    errorText.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao verificar licença.", Toast.LENGTH_LONG).show()
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDataValida(data: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dataLicenca = LocalDate.parse(data, formatter)
            dataLicenca.isAfter(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }
}
