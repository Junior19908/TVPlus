package com.skysinc.tvplus.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skysinc.tvplus.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _licenseExpiration = MutableStateFlow<Date?>(null)
    val licenseExpiration: StateFlow<Date?> = _licenseExpiration

    init {
        // Primeiro, carrega valores salvos localmente (offline)
        _licenseExpiration.value = getLocalLicenseExpiration()

        // Atualiza em segundo plano se possível
        fetchLicenseExpiration()

        // Carrega canais
        fetchChannels()
    }

    /**
     * Busca a chave de licença salva localmente.
     */
    private fun getLicenseKey(): String? {
        val prefs = context.getSharedPreferences("TVPlusPrefs", Context.MODE_PRIVATE)
        return prefs.getString("license_key", null)
    }

    /**
     * Busca a data de expiração salva localmente.
     */
    private fun getLocalLicenseExpiration(): Date? {
        val prefs = context.getSharedPreferences("TVPlusPrefs", Context.MODE_PRIVATE)
        val expMillis = prefs.getLong("license_expiration", 0L)
        return if (expMillis > 0) Date(expMillis) else null
    }

    /**
     * Busca e atualiza a data de expiração da licença a partir do Firestore.
     */
    private fun fetchLicenseExpiration() {
        val licenseKey = getLicenseKey() ?: return

        viewModelScope.launch {
            try {
                val snapshot = Firebase.firestore.collection("licencas")
                    .whereEqualTo("chave", licenseKey)
                    .limit(1)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val dataExp = doc.getTimestamp("dataExpiracao")?.toDate()

                    // Atualiza valor em memória
                    _licenseExpiration.value = dataExp

                    // Atualiza valor no SharedPreferences (cache offline)
                    val prefs = context.getSharedPreferences("TVPlusPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putLong("license_expiration", dataExp?.time ?: 0L)
                        .apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Carrega os canais do Firestore.
     */
    private fun fetchChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = Firebase.firestore.collection("canais")
                    .whereEqualTo("ativo", true)
                    .get()
                    .await()

                _channels.value = snapshot.documents.mapNotNull { doc ->
                    val nome = doc.getString("nome")
                    val url = doc.getString("streamUrl")
                    val thumb = doc.getString("thumb")
                    val cat = doc.getString("categoria") ?: "Geral"
                    if (nome != null && url != null && thumb != null) {
                        Channel(nome, url, thumb, cat)
                    } else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _channels.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
