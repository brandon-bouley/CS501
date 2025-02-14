package com.example.flashcardquiz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser
import com.example.flashcardquiz.ui.theme.FlashcardQuizTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.io.path.name

/**
 * The main activity for the Flashcard Quiz application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                FlashcardApp(context = LocalContext.current)
            }
        }
    }
}

/**
 * Data class representing a single flashcard.
 */
data class Flashcard(
    val question: String,
    val answer: String
)

/**
 * Parses the flashcards.xml file and returns a list of [Flashcard] objects.
 *
 * This function reads the XML file located in the `res/xml` directory and extracts
 * the `question` and `answer` attributes from each `<card>` tag.
 *
 */
fun parseFlashcardsXml(context: Context): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    val parser = context.resources.getXml(R.xml.flashcards)

    // Map to store attribute values for each card
    val attributeMap = mutableMapOf<String, String>()

    var eventType = parser.eventType

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                if (parser.name == "card") {
                    // Clear the map for each new card
                    attributeMap.clear()
                    // Iterate through attributes and store them in the map
                    for (i in 0 until parser.attributeCount) {
                        val attributeName = parser.getAttributeName(i)
                        val attributeValue = parser.getAttributeValue(i)
                        attributeMap[attributeName] = attributeValue
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "card") {
                    // Extract values from the map
                    val question = attributeMap["question"] ?: ""
                    val answer = attributeMap["answer"] ?: ""
                    // Add the flashcard to the list
                    flashcards.add(Flashcard(question, answer))
                }
            }
        }
        eventType = parser.next()
    }
    return flashcards
}

/**
 * Composable function that displays the main flashcard app.
 *
 * This function displays a list of flashcards in a [LazyRow] and shuffles them every 15 seconds.
 *
 */
@Composable
fun FlashcardApp(context: Context, modifier: Modifier = Modifier) {
    // Parse the XML and remember the list of flashcards
    val flashcards = remember { parseFlashcardsXml(context) }
    // State to hold the shuffled list of flashcards
    var shuffledFlashcards by remember { mutableStateOf(flashcards) }

    // Shuffle flashcards every 15 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            shuffledFlashcards = shuffledFlashcards.shuffled()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp))
        {
            items(shuffledFlashcards) { flashcard ->
                FlashcardItem(flashcard = flashcard)
            }
        }
    }
}

/**
 * Composable function that displays a single flashcard item.
 *
 * This function displays a card with a question on one side and an answer on the other.
 * The card can be flipped to reveal the answer.
 *
 */
@Composable
fun FlashcardItem(flashcard: Flashcard) {
    // State to track if the card is flipped
    var isFlipped by remember { mutableStateOf(false) }
    // Animate the card's rotation based on the isFlipped state
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    // Get the screen width to set the card width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .width(screenWidth) // Set card width to match screen width
            .aspectRatio(1.5f) // Maintain aspect ratio (height = width / 1.5)
            .clickable { isFlipped = !isFlipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // Front of the card
        if (rotation <= 90f) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flashcard.question,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        // Back of the card
        else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f // Rotate the back of the card to prevent mirroring
                    },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flashcard.answer,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Preview function for the [FlashcardApp] composable.
 */
@Preview(showBackground = true)
@Composable
fun FlashcardAppPreview() {
    FlashcardQuizTheme {
        FlashcardApp(context = LocalContext.current)
    }
}