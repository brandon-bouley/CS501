import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photogalleryapp.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoGalleryScreen()
        }
    }
}

@Composable
fun PhotoGalleryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Photo Gallery",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)

        )

        // Grid of Images
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GalleryItem(
                    imageRes = R.drawable.img1,
                    caption = "Alley in Fenway"
                )
                Spacer(modifier = Modifier.width(8.dp))
                GalleryItem(
                    imageRes = R.drawable.img2,
                    caption = "My friends and I"
                )
                Spacer(modifier = Modifier.width(8.dp))
                GalleryItem(
                    imageRes = R.drawable.img3,
                    caption = "His and Hers"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GalleryItem(
                    imageRes = R.drawable.img4,
                    caption = "Stand-up Comedy"
                )
                Spacer(modifier = Modifier.width(8.dp))
                GalleryItem(
                    imageRes = R.drawable.img5,
                    caption = "Same alley"
                )
                Spacer(modifier = Modifier.width(8.dp))
                GalleryItem(
                    imageRes = R.drawable.img6,
                    caption = "Thanksgiving"
                )
            }
        }
    }
}

@Composable
fun GalleryItem(imageRes: Int, caption: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = caption,
            modifier = Modifier
                .size(100.dp)
                .background(Color.LightGray)
        )

        // Caption
        Text(
            text = caption,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhotoGalleryScreen() {
    PhotoGalleryScreen()
}