package com.skysinc.tvplus.ui

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
import com.skysinc.tvplus.ui.theme.MyTVAppTheme
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object LicenseInfo {
    val dataExpiracao = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("2025-07-12")!!
}

class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyTVAppTheme {
                    var playingVideoUrl by remember { mutableStateOf<String?>(null) }
                    val hoje = Date()
                    val diasRestantes = ((LicenseInfo.dataExpiracao.time - hoje.time) / (1000 * 60 * 60 * 24)).toInt()
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = sdf.format(LicenseInfo.dataExpiracao)

                    val channels by produceState(initialValue = emptyList<Channel>()) {
                        val snapshot = Firebase.firestore.collection("canais")
                            .whereEqualTo("ativo", true)
                            .get()
                            .await()
                        value = snapshot.documents.mapNotNull { doc ->
                            val nome = doc.getString("nome")
                            val url = doc.getString("streamUrl")
                            val thumb = doc.getString("thumb")
                            val cat = doc.getString("categoria") ?: ""
                            if (nome != null && url != null && thumb != null) {
                                Channel(nome, url, thumb, cat)
                            } else null
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        if (playingVideoUrl != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                VideoPlayer(
                                    modifier = Modifier.fillMaxSize(),
                                    videoUrl = playingVideoUrl!!
                                )
                                BackHandler { playingVideoUrl = null }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
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

                                    TVGuideGrid(
                                        channels = channels,
                                        onChannelClick = { channel ->
                                            playingVideoUrl = channel.streamUrl
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(
                                            color = Color(0xFFFFA500),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TVGuideGrid(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(220.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCard(
                channel = channel,
                onChannelClick = { onChannelClick(channel) }
            )
        }
    }
}

@Composable
fun ChannelCard(
    channel: Channel,
    onChannelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .background(Color.DarkGray)
            .clickable(onClick = onChannelClick)
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
