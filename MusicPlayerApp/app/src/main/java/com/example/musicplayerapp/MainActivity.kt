import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayerapp.R

@Composable
fun MusicPlayerScreen(
    songTitle: String = "Sympathy is a knife",
    artistName: String = "Charli Xcx",
    albumCoverResId: Int = R.drawable.brat,
    onPreviousClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Album Cover
            Image(
                painter = painterResource(id = albumCoverResId),
                contentDescription = "Album Cover",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )

            // Song Title
            Text(
                text = songTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Artist Name
            Text(
                text = artistName,
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Previous Button
                PlaybackControlButton(
                    iconResId = R.drawable.prev, 
                    contentDescription = "Skip Previous",
                    onClick = onPreviousClick
                )

                // Play Button
                PlaybackControlButton(
                    iconResId = R.drawable.play,
                    contentDescription = "Play",
                    onClick = onPlayClick
                )

                // Pause Button
                PlaybackControlButton(
                    iconResId = R.drawable.pause,
                    contentDescription = "Pause",
                    onClick = onPauseClick
                )

                // Skip Next Button
                PlaybackControlButton(
                    iconResId = R.drawable.skip,
                    contentDescription = "Skip Next",
                    onClick = onNextClick
                )
            }
        }
    }
}

@Composable
fun PlaybackControlButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MusicPlayerScreenPreview() {
    MusicPlayerScreen()
}