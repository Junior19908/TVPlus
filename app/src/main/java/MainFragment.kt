package com.example.livetv.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter
import com.example.livetv.Channel
import com.example.livetv.ui.theme.MyTVAppTheme

class MainFragment : Fragment() {

    private val categories = listOf("Filmes", "Notícias", "Infantil")

    private val channels = listOf(
        Channel(
            "BR| A&E HD",
            "http://zkbvzkj.megahdtv.xyz:80/live/1103581436/4099381829/171324.m3u8",
            "https://lo1.in/BR/ane.png",
            "Filmes"
        ),
        Channel(
            "Canal 2",
            "http://stream2.m3u8",
            "https://via.placeholder.com/300x200.png?text=Canal+2",
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
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "TVPlus - Canais ao Vivo",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                            TVGuideGrid(channels)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TVGuideGrid(channels: List<Channel>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(220.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCard(channel)
        }
    }
}

@Composable
fun ChannelCard(channel: Channel) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .background(Color.DarkGray)
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
