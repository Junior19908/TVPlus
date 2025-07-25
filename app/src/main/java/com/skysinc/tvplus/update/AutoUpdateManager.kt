package com.skysinc.tvplus.update

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AutoUpdateManager(private val activity: Activity) {

    private val db = Firebase.firestore

    companion object {
        const val REQUEST_UNKNOWN_APP_SOURCES = 2001
    }

    /**
     * Verifica se há uma nova versão disponível no Firestore.
     */
    fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = db.collection("updates").document("app").get().await()
                val latestVersionCode = doc.getLong("latestVersionCode")?.toInt() ?: 0
                val latestVersionName = doc.getString("latestVersionName") ?: ""
                val apkUrl = doc.getString("apkUrl") ?: ""

                val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                val currentVersionCode = if (Build.VERSION.SDK_INT >= 28) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                if (latestVersionCode > currentVersionCode) {
                    withContext(Dispatchers.Main) {
                        showUpdateDialog(latestVersionName, apkUrl)
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoUpdate", "Erro ao buscar atualização", e)
            }
        }
    }

    /**
     * Exibe um diálogo para confirmar a atualização.
     */
    private fun showUpdateDialog(versionName: String, apkUrl: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("Nova atualização disponível")
            setMessage("Versão $versionName disponível. Deseja atualizar agora?")
            setCancelable(false)
            setPositiveButton("Atualizar") { _, _ -> downloadAndInstall(apkUrl) }
            setNegativeButton("Mais tarde", null)
            show()
        }
    }

    /**
     * Converte uma URL gs:// em uma URL HTTP de download.
     */
    private suspend fun resolveDownloadUrl(apkUrl: String): String {
        return if (apkUrl.startsWith("gs://")) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(apkUrl)
            storageRef.downloadUrl.await().toString()
        } else {
            apkUrl
        }
    }

    /**
     * Faz o download do APK e mostra uma barra de progresso.
     */
    private fun downloadAndInstall(apkUrl: String) {
        val progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            isIndeterminate = false
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Baixando atualização...")
            .setView(progressBar)
            .setCancelable(false)
            .create()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resolvedUrl = resolveDownloadUrl(apkUrl)
                val url = URL(resolvedUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connect()

                val lengthOfFile = conn.contentLength
                val file = File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
                val fos = FileOutputStream(file)

                var total = 0L
                val buffer = ByteArray(4096)
                var count: Int

                withContext(Dispatchers.Main) {
                    dialog.show()
                }

                conn.inputStream.use { input ->
                    fos.use { output ->
                        while (input.read(buffer).also { count = it } != -1) {
                            output.write(buffer, 0, count)
                            total += count
                            val progress = (total * 100 / lengthOfFile).toInt()
                            withContext(Dispatchers.Main) {
                                progressBar.progress = progress
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    installApk(file, resolvedUrl)
                }
            } catch (e: Exception) {
                Log.e("AutoUpdate", "Erro no download", e)
                withContext(Dispatchers.Main) { dialog.dismiss() }

                logUpdate("failed", "unknown", apkUrl)
            }
        }
    }

    /**
     * Solicita permissão (se necessário) e instala o APK.
     */
    private fun installApk(file: File, apkUrl: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val canInstall = activity.packageManager.canRequestPackageInstalls()
                    if (!canInstall) {
                        AlertDialog.Builder(activity).apply {
                            setTitle("Permissão necessária")
                            setMessage("Para atualizar o app, é necessário permitir a instalação de fontes desconhecidas.")
                            setCancelable(false)
                            setPositiveButton("Permitir") { _, _ ->
                                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                intent.data = Uri.parse("package:${activity.packageName}")
                                activity.startActivityForResult(intent, REQUEST_UNKNOWN_APP_SOURCES)
                            }
                            setNegativeButton("Cancelar", null)
                            show()
                        }
                        return@launch
                    }
                }
                performApkInstallation(file, apkUrl)
            } catch (e: Exception) {
                Log.e("AutoUpdate", "Erro na instalação", e)
            }
        }
    }

    /**
     * Executa a instalação do APK.
     */
    private fun performApkInstallation(file: File, apkUrl: String) {
        val apkUri: Uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)

        // Log de sucesso (registro no Firestore)
        val versionName = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
        logUpdate("success", versionName.toString(), apkUrl)
    }

    /**
     * Registra log da atualização no Firestore.
     */
    private fun logUpdate(status: String, versionInstalled: String, apkUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = activity.getSharedPreferences("TVPlusPrefs", Activity.MODE_PRIVATE)
                val licenseKey = prefs.getString("license_key", "DESCONHECIDA") ?: "DESCONHECIDA"

                val logData = mapOf(
                    "licenseKey" to licenseKey,
                    "versionInstalled" to versionInstalled,
                    "apkUrl" to apkUrl,
                    "deviceId" to Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID),
                    "updateDate" to Timestamp.now(),
                    "status" to status
                )
                Firebase.firestore.collection("updates_logs").add(logData).await()
                Log.i("AutoUpdate", "Log de atualização registrado com sucesso.")
            } catch (e: Exception) {
                Log.e("AutoUpdate", "Erro ao registrar log de atualização", e)
            }
        }
    }
}
