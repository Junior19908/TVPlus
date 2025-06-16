package com.skysinc.tvplus

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

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
        val currentDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        db.collection("licencas")
            .whereEqualTo("chave", codigo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val docRef = doc.reference

                    val ativo = doc.getBoolean("ativo") ?: false
                    val dataExp = doc.getTimestamp("dataExpiracao")?.toDate()
                    val registeredDeviceId = doc.getString("deviceId")
                    val dataAtivacao = doc.getString("dataAtivacao")

                    if (!ativo) {
                        errorText.text = "Licença desativada."
                        errorText.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    if (dataExp == null || dataExp.before(Date())) {
                        errorText.text = "Licença expirada."
                        errorText.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    // Primeira ativação
                    if (registeredDeviceId.isNullOrEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val nowFormatted = sdf.format(Date())

                        docRef.update(
                            mapOf(
                                "deviceId" to currentDeviceId,
                                "dataAtivacao" to nowFormatted
                            )
                        ).addOnSuccessListener {
                            salvarLoginLocalEEntrar()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Erro ao salvar ativação.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Dispositivo autorizado
                    else if (registeredDeviceId == currentDeviceId) {
                        salvarLoginLocalEEntrar()
                    }
                    // Tentativa de outro dispositivo
                    else {
                        errorText.text = "Chave já usada em outro dispositivo."
                        errorText.visibility = View.VISIBLE
                    }
                } else {
                    errorText.text = "Chave não encontrada."
                    errorText.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao verificar licença.", Toast.LENGTH_LONG).show()
                Log.e("LOGIN_FIREBASE", "Falha: ${it.message}", it)
            }
    }

    private fun salvarLoginLocalEEntrar() {
        val prefs = getSharedPreferences("TVPlusPrefs", MODE_PRIVATE)
        prefs.edit().putLong("login_time", System.currentTimeMillis()).apply()

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
