package com.example.livetv.ui

import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter
import com.example.livetv.Channel
import com.example.livetv.VideoPlayer
import com.example.livetv.ui.LoginManager.loginTimestmap
import com.example.livetv.ui.theme.MyTVAppTheme
import java.util.concurrent.TimeUnit


object LoginManager{
    var loginTimestmap: Long = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        private set
    fun mockLoginNow(){
        loginTimestmap = System.currentTimeMillis()
    }
    const val LOGIN_EXPIRATION_HOURS = 48 * 60 * 60 * 1000L //48 hora em millissegundos
}

class MainFragment : Fragment() {

    private val categories = listOf("Filmes", "Notícias", "Infantil")

    // Certifique-se de que a sua classe Channel está acessível (pode precisar de import)
    // Se Channel estiver em outro pacote, ajuste o import: import com.example.livetv.model.Channel
    private val channels = listOf(
        Channel(
            "BR| A&E FHD",
            "http://zkbvzkj.megahdtv.xyz:80/live/1103581436/4099381829/162298.m3u8",
            "https://lo1.in/BR/ane.png",
            "Geral"
        ),
        Channel(
            "BR| GLOBO NEWS HD",
            "http://zkbvzkj.megahdtv.xyz:80/live/1103581436/4099381829/162406.m3u8",
            "https://lo1.in/BR/globi.png",
            "Notícias"
        )
        // Adicione mais canais conforme necessário
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyTVAppTheme {
                    // Estado para controlar qual URL de vídeo está sendo reproduzida
                    // String vazia significa que nenhum vídeo está sendo reproduzido
                    var playingVideoUrl by remember { mutableStateOf<String?>(null) }
                    var timeLeftForLoginText by remember { mutableStateOf("Calculando...") }
                    var loginTimestamp = loginTimestmap + LoginManager.LOGIN_EXPIRATION_PERIOD_MS

                    DisposableEffect(key1 = Unit) {
                            val timer = object : CountDownTimer(expirationTime - System.currentTimeMillis(),1000) {
                                override fun onTick(millisUntilFinished: Long) {

                                    timeLeftForLoginText = formatMillisToTimeLeft(millisUntilFinished)
                                }
                            }
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        if (playingVideoUrl != null) {
                            // Tela do Player
                            Box(modifier = Modifier.fillMaxSize()) {
                                VideoPlayer(
                                    modifier = Modifier.fillMaxSize(),
                                    videoUrl = playingVideoUrl!!
                                )
                                // Adicionar um BackHandler para permitir voltar da tela do player
                                BackHandler {
                                    playingVideoUrl = null // Volta para a grade de canais
                                }
                            }
                        } else {
                            // Tela da Grade de Canais
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "TVPlus - Canais ao Vivo",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                                TVGuideGrid(
                                    channels = channels,
                                    onChannelClick = { channel ->
                                        // Quando um canal é clicado, define a URL para reprodução
                                        playingVideoUrl = channel.streamUrl
                                    }
                                )
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
    onChannelClick: (Channel) -> Unit // Callback para quando um canal é clicado
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(220.dp), // Ajuste o tamanho mínimo do item conforme necessário
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCard(
                channel = channel,
                onChannelClick = { onChannelClick(channel) } // Passa o clique para cima
            )
        }
    }
}

@Composable
fun ChannelCard(
    channel: Channel,
    onChannelClick: () -> Unit // Callback para o clique
) {
    Column(
        modifier = Modifier
            .width(220.dp) // Ajuste a largura se necessário
            .background(Color.DarkGray)
            .clickable(onClick = onChannelClick) // Torna o card clicável
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(channel.thumbnailUrl),
            contentDescription = channel.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Ajuste a altura se necessário
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = channel.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}