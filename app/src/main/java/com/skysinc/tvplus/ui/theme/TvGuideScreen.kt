import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.skysinc.tvplus.Channel // Usa o Channel correto do seu projeto

// Program apenas para exibir detalhes do conteúdo, se necessário
data class Program(
    val title: String,
    val startTime: String,
    val durationMinutes: Int,
    val description: String,
    val thumbnailUrl: String
)

@Composable
fun EPGScreen(
    channels: List<Channel>,
    selectedCategory: String,
    onChannelClick: (Channel) -> Unit
) {
    val filteredChannels = channels.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Programação - $selectedCategory",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )

        LazyColumn {
            items(filteredChannels) { channel ->
                EPGChannelRow(channel = channel, onClick = { onChannelClick(channel) })
            }
        }
    }
}

@Composable
fun EPGChannelRow(channel: Channel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .background(Color(0xFF2C2C2C))
            .padding(12.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = channel.thumbnailUrl),
            contentDescription = channel.name,
            modifier = Modifier
                .width(80.dp)
                .height(60.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(channel.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Em exibição: Exemplo de programa", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ProgramCard(program: Program, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width((program.durationMinutes * 2).dp)
            .background(Color.DarkGray)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(text = program.startTime, color = Color.Gray, fontSize = 12.sp)
        Text(text = program.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ProgramDetail(program: Program) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(program.thumbnailUrl),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .padding(end = 16.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(program.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(program.description, color = Color.LightGray, fontSize = 14.sp)
        }
    }
}
