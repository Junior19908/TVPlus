package com.skysinc.tvplus.ui

import android.os.Bundle
import android.os.CountDownTimer // Import para o CountDownTimer
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
import androidx.compose.ui.unit.sp // Para tamanho da fonte
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter
import com.skysinc.tvplus.Channel
import com.skysinc.tvplus.VideoPlayer // Supondo que você tenha este import
import com.skysinc.tvplus.ui.theme.MyTVAppTheme
import java.util.concurrent.TimeUnit // Import para TimeUnit
import androidx.compose.foundation.lazy.grid.items

// Simulação de onde você pegaria o timestamp do login
// Em um app real, isso viria de SharedPreferences, ViewModel, etc.
object LoginManager {
    // Para teste, vamos definir um timestamp de login como se tivesse ocorrido há X horas.
    // Exemplo: Login ocorreu há 1 hora
    var loginTimestamp: Long = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        private set // Para que não seja modificado externamente neste exemplo simples

    fun mockLoginNow() { // Função para simular um novo login
        loginTimestamp = System.currentTimeMillis()
    }

    const val LOGIN_EXPIRATION_PERIOD_MS = 48 * 60 * 60 * 1000L // 48 horas em milissegundos
}


class MainFragment : Fragment() {

    private val categories = listOf("Filmes", "Notícias", "Infantil")
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
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyTVAppTheme {
                    var playingVideoUrl by remember { mutableStateOf<String?>(null) }

                    // Estado para o texto do tempo restante
                    var timeLeftForLoginText by remember { mutableStateOf("Calculando...") }

                    // Lógica para atualizar o tempo restante periodicamente
                    // Usaremos um CountDownTimer simples para este exemplo,
                    // mas em um cenário mais robusto, um Flow ou LiveData de um ViewModel seria melhor.
                    val loginTimestamp = LoginManager.loginTimestamp
                    val expirationTime = loginTimestamp + LoginManager.LOGIN_EXPIRATION_PERIOD_MS

                    DisposableEffect(key1 = Unit) { // Executa uma vez e limpa ao sair
                        val timer = object : CountDownTimer(expirationTime - System.currentTimeMillis(), 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                timeLeftForLoginText = formatMillisToTimeLeft(millisUntilFinished)
                            }

                            override fun onFinish() {
                                timeLeftForLoginText = "Login necessário!"
                                // Aqui você poderia acionar a lógica de logout ou navegação para tela de login
                            }
                        }
                        if (expirationTime > System.currentTimeMillis()) {
                            timer.start()
                        } else {
                            timeLeftForLoginText = "Login necessário!"
                        }

                        onDispose {
                            timer.cancel()
                        }
                    }


                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        if (playingVideoUrl != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                VideoPlayer(
                                    modifier = Modifier.fillMaxSize(),
                                    videoUrl = playingVideoUrl!!
                                )
                                BackHandler {
                                    playingVideoUrl = null
                                }
                            }
                        } else {
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
                                    // Botão para simular novo login (APENAS PARA TESTE)
                                    Text(
                                        text = "Relogar (Teste)",
                                        modifier = Modifier.clickable {
                                            LoginManager.mockLoginNow()
                                            // Forçar recomposição ou reinício do timer seria necessário aqui
                                            // Uma forma simples é navegar para si mesmo ou usar um estado para trigger
                                            // Para este exemplo, a mudança no timestamp não vai reiniciar o timer
                                            // automaticamente sem uma lógica de recomposição mais explícita.
                                            // Idealmente, o timestamp viria de um StateFlow/LiveData.
                                            // Se você quer que o timer reinicie imediatamente, precisaria
                                            // de uma chave no DisposableEffect que mude com o loginTimestamp.
                                            // Mas para este exemplo, vamos manter simples.
                                        }.padding(8.dp).background(Color.Gray),
                                        color = Color.White
                                    )
                                }

                                // Exibe o tempo restante para o login
                                Text(
                                    text = "Próximo login em: $timeLeftForLoginText",
                                    color = Color.Yellow, // Cor de destaque
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 8.dp)
                                )

                                TVGuideGrid(
                                    channels = channels,
                                    onChannelClick = { channel ->
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

// Função utilitária para formatar o tempo
fun formatMillisToTimeLeft(millis: Long): String {
    if (millis < 0) return "Expirado"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

// ... (Restante do seu código: TVGuideGrid, ChannelCard, etc.)
// Lembre-se de importar VideoPlayer se estiver em outro pacote
// Lembre-se de ter a classe Channel definida ou importada

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