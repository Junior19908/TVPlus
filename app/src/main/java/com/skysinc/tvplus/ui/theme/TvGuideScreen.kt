import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

// Sample data

data class Program(val title: String, val startTime: String, val durationMinutes: Int, val description: String, val thumbnailUrl: String)
data class Channel(val name: String, val programs: List<Program>)

@Composable
fun EPGScreen(channels: List<Channel>) {
    var selectedProgram by remember { mutableStateOf<Program?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Header with program details
        selectedProgram?.let { program ->
            ProgramDetail(program)
        } ?: Box(modifier = Modifier.height(160.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(channels) { channel ->
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )

                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(channel.programs) { program ->
                        ProgramCard(program = program, onClick = { selectedProgram = program })
                    }
                }
            }
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
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .padding(8.dp), verticalAlignment = Alignment.CenterVertically
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
