package com.skysinc.tvplus.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skysinc.tvplus.Channel
import com.skysinc.tvplus.VideoPlayer
import com.skysinc.tvplus.update.AutoUpdateManager
import com.skysinc.tvplus.ui.theme.MyTVAppTheme
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()
        AutoUpdateManager(activity).checkForUpdates()

        // Obtém a versão do app
        val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        val appVersion = packageInfo.versionName

        return ComposeView(requireContext()).apply {
            setContent {
                MyTVAppTheme {
                    var playingVideoUrl by remember { mutableStateOf<String?>(null) }

                    // Obter data de expiração salva no SharedPreferences
                    val prefs = activity.getSharedPreferences("TVPlusPrefs", Context.MODE_PRIVATE)
                    val expirationMillis = prefs.getLong("license_expiration", 0L)
                    val expirationDate = if (expirationMillis > 0) Date(expirationMillis) else null

                    val diasRestantes = expirationDate?.let {
                        ((it.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                    } ?: 0

                    val dataFormatada = expirationDate?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "N/A"

                    // Carrega canais do Firestore
                    val channels by produceState(initialValue = emptyList<Channel>()) {
                        try {
                            val snapshot = Firebase.firestore.collection("canais")
                                .whereEqualTo("ativo", true)
                                .get()
                                .await()
                            value = snapshot.documents.mapNotNull { doc ->
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
                            value = emptyList()
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (playingVideoUrl != null) {
                                // Tela do Player
                                VideoPlayer(
                                    modifier = Modifier.fillMaxSize(),
                                    videoUrl = playingVideoUrl!!
                                )
                                BackHandler { playingVideoUrl = null }
                            } else {
                                // Tela principal
                                Column(modifier = Modifier.fillMaxSize()) {
                                    TopBar()
                                    TVGuideGrid(channels) { selected ->
                                        playingVideoUrl = selected.streamUrl
                                    }
                                }
                            }

                            // Banner de expiração (sempre visível)
                            ExpirationBanner(dataFormatada, diasRestantes)

                            // Versão do app no canto inferior esquerdo
                            Text(
                                text = "Versão $appVersion",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TVPlus - Canais ao Vivo",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}

@Composable
fun ExpirationBanner(dataFormatada: String, diasRestantes: Int) {
    val bannerColor = if (diasRestantes in 1..3) Color.Red else Color(0xFFFFA500)

    Box(
        modifier = Modifier
            .padding(12.dp)
            .background(
                color = Color(0xFFFFA500),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Expira em: $dataFormatada",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$diasRestantes dias restantes",
                color = Color.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun TVGuideGrid(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(220.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCard(channel = channel) { onChannelClick(channel) }
        }
    }
}

@Composable
fun ChannelCard(channel: Channel, onChannelClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .background(Color.DarkGray)
            .clickable { onChannelClick() }
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(channel.thumbnailUrl),
            contentDescription = channel.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = channel.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
