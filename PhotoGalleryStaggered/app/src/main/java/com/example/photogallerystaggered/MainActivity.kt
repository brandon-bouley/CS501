import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.xmlpull.v1.XmlPullParser
import com.example.photogallerystaggered.R

/**
 * The main activity of the Staggered Photo Gallery app.
 * Sets up the UI and displays the photo gallery.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        PhotoGallery(
                            context = this,
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(top = 56.dp)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Data class representing a photo with a file name and a title.
 */
data class Photo(
    val fileName: String,
    val title: String
)

/**
 * Parses the photos.xml file and returns a list of Photo objects.
 */
fun parsePhotosXml(context: Context): List<Photo> {
    val photos = mutableListOf<Photo>()
    // Get the XML parser for the photos.xml resource
    val parser = context.resources.getXml(R.xml.photos)

    var eventType = parser.eventType
    var fileName: String? = null
    var title: String? = null

    // Iterate through the XML events
    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                // Check for the start of a "photo" tag
                when (parser.name) {
                    "photo" -> {
                        // Extract the fileName and title attributes
                        fileName = parser.getAttributeValue(null, "fileName")
                        title = parser.getAttributeValue(null, "title")
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                // Check for the end of a "photo" tag
                if (parser.name == "photo" && fileName != null && title != null) {
                    // Add a new Photo object to the list
                    photos.add(Photo(fileName, title))
                    // Reset the variables for the next photo
                    fileName = null
                    title = null
                }
            }
        }
        // Move to the next XML event
        eventType = parser.next()
    }
    return photos
}

/**
 * Composable function that displays a grid of photos.
 *
 */
@Composable
fun PhotoGallery(context: Context, modifier: Modifier = Modifier) {
    // Parse the XML and remember the list of photos
    val photos = remember { parsePhotosXml(context) }

    // Display a vertical grid of photos
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        // Display each photo in the grid
        items(photos) { photo ->
            PhotoItem(photo = photo)
        }
    }
}

/**
 * Composable function that displays a single photo item.
 */
@Composable
fun PhotoItem(photo: Photo) {
    // State to track if the image is enlarged
    var isEnlarged by remember { mutableStateOf(false) }
    // Animate the image size based on the isEnlarged state
    val imageSize by animateDpAsState(
        targetValue = if (isEnlarged) 300.dp else 150.dp,
        animationSpec = tween(durationMillis = 500)
    )

    // Use map to look up the resource ID
    val imageResId = imageMap[photo.fileName] ?: R.drawable.notfound

    // Display a card with the photo and title
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { isEnlarged = !isEnlarged },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        // Box to center the image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageSize),
            contentAlignment = Alignment.Center
        ) {
            // Display the image
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = photo.title,
                modifier = Modifier
                    .size(imageSize)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
        // Display the photo title
        Text(
            text = photo.title,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Preview function for the PhotoGallery composable.
 */
@Preview(showBackground = true)
@Composable
fun PhotoGalleryPreview() {
    MaterialTheme {
        PhotoGallery(context = LocalContext.current)
    }
}

/**
 * Map image names to resource IDs
 */
val imageMap = mapOf(
    "img1" to R.drawable.img1,
    "img2" to R.drawable.img2,
    "img3" to R.drawable.img3,
    "img4" to R.drawable.img4,
    "img5" to R.drawable.img5,
    "img6" to R.drawable.img6
)