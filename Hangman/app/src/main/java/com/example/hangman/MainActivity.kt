// MainActivity.kt
package com.example.hangman

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangman.ui.theme.HangmanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HangmanTheme {
                HangmanGameScreen()
            }
        }
    }
}

// region Data Models
/**
 * Represents a word and its associated hint
 * @property value The actual word to guess
 * @property hint Clue to help guess the word
 */
data class GameWord(val value: String, val hint: String)

/**
 * Holds the complete state of the Hangman game
 * @property word Current word being guessed
 * @property guessedLetters Set of letters already guessed
 * @property incorrectGuesses Number of wrong guesses made
 * @property hintCount Number of hints used
 * @property isGameOver Flag if game has ended
 * @property isWon Flag if player won the game
 */
data class GameState(
    val word: GameWord,
    val guessedLetters: Set<Char>,
    val incorrectGuesses: Int,
    val hintCount: Int,
    val isGameOver: Boolean,
    val isWon: Boolean
)
// endregion

// region State Management
/**
 * Creates a new game state with random word selection
 * @param words Pool of possible words to choose from
 */
private fun createNewGameState(words: List<GameWord>): GameState {
    val randomWord = words.random()
    return GameState(
        word = randomWord,
        guessedLetters = emptySet(),
        incorrectGuesses = 0,
        hintCount = 0,
        isGameOver = false,
        isWon = false
    )
}

/**
 * Updates game state after a letter guess
 * @param gameState Current game state
 * @param letter Guessed letter
 */
private fun updateGameState(gameState: GameState, letter: Char): GameState {
    val newGuessedLetters = gameState.guessedLetters + letter
    val isCorrect = gameState.word.value.contains(letter)

    return gameState.copy(
        guessedLetters = newGuessedLetters,
        incorrectGuesses = if (!isCorrect) gameState.incorrectGuesses + 1 else gameState.incorrectGuesses,
        isGameOver = newGuessedLetters.containsAll(gameState.word.value.toSet()) ||
                (gameState.incorrectGuesses == 6 && !isCorrect),
        isWon = newGuessedLetters.containsAll(gameState.word.value.toSet())
    )
}

/**
 * Custom saver for GameState to survive configuration changes
 */
object GameStateSaver : Saver<GameState, List<Any>> {
    override fun restore(value: List<Any>): GameState {
        println("Restoring state: $value")
        return GameState(
            word = GameWord(value[0] as String, value[1] as String),
            guessedLetters = (value[2] as String).toSet(),
            incorrectGuesses = value[3] as Int,
            hintCount = value[4] as Int,
            isGameOver = value[5] as Boolean,
            isWon = value[6] as Boolean
        )
    }

    override fun SaverScope.save(value: GameState): List<Any> {
        val savedState = listOf(
            value.word.value,
            value.word.hint,
            value.guessedLetters.joinToString(""),
            value.incorrectGuesses,
            value.hintCount,
            value.isGameOver,
            value.isWon
        )
        println("Saving state: $savedState")
        return savedState
    }
}
// endregion

// region Main Game Screen
/**
 * Main composable hosting the Hangman game
 */
@Composable
fun HangmanGameScreen() {
    val words = listOf(
        GameWord("COMPOSE", "Google's modern UI toolkit"),
        GameWord("ANDROID", "Commonly used OS"),
        GameWord("KOTLIN", "Coding language for Android apps"),
        GameWord("JETPACK", "Suite of libraries"),
        GameWord("HANGMAN", "Very meta"),
        GameWord("TURING", "Last name of the father of CS")
    )

    var gameState by rememberSaveable(stateSaver = GameStateSaver) {
        mutableStateOf(createNewGameState(words))
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope() // Create a CoroutineScope

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        GameLayout(
            gameState = gameState,
            onNewGame = { gameState = createNewGameState(words) },
            onLetterGuess = { letter -> gameState = updateGameState(gameState, letter) },
            onHintClick = {
                handleHintRequest(
                    gameState = gameState,
                    updateState = { gameState = it },
                    showSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            },
            isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
            modifier = Modifier.padding(padding)
        )
    }
}

// endregion

// region Game Layout Components
/**
 * Root layout that switches between portrait/landscape configurations
 */
@Composable
private fun GameLayout(
    gameState: GameState,
    onNewGame: () -> Unit,
    onLetterGuess: (Char) -> Unit,
    onHintClick: () -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Row(modifier.fillMaxSize()) {
            // Left Panel: Input Controls
            Column(Modifier.weight(1f)) {
                LetterGrid(
                    gameState = gameState,
                    onLetterClick = onLetterGuess,
                    modifier = Modifier.weight(1f)
                )
                // Display hint on landscape
                HintSection(
                    gameState = gameState,
                    onHintClick = onHintClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Right Panel: Game Display
            GameStatusPanel(
                gameState = gameState,
                onNewGame = onNewGame,
                modifier = Modifier.weight(1f) )
        }
    } else {
        Column(modifier.fillMaxSize()) {
            // Top Section: Game Display
            GameStatusPanel(
                gameState = gameState,
                onNewGame = onNewGame,
                modifier = Modifier.weight(2f))

            // Bottom Section: Input Controls
            LetterGrid(
                gameState = gameState,
                onLetterClick = onLetterGuess,
                modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Displays game state including hangman image, word progress, and new game button
 */
@Composable
private fun GameStatusPanel(
    gameState: GameState,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hangman Image Display
        when (gameState.incorrectGuesses) {
            0 -> HangmanImage(R.drawable.hangman0, "Initial state")
            1 -> HangmanImage(R.drawable.hangman1, "Head added")
            2 -> HangmanImage(R.drawable.hangman2, "Body added")
            3 -> HangmanImage(R.drawable.hangman3, "Left arm")
            4 -> HangmanImage(R.drawable.hangman4, "Right arm")
            5 -> HangmanImage(R.drawable.hangman5, "Left leg")
            6 -> HangmanImage(R.drawable.hangman6, "Full hangman")
        }

        Spacer(Modifier.height(16.dp))

        // Word Progress Display
        Text(
            text = gameState.word.value.map { letter ->
                if (gameState.guessedLetters.contains(letter)) letter else '_'
            }.joinToString(" "),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Game Result Message
        if (gameState.isGameOver) {
            Text(
                text = if (gameState.isWon) "Victory!" else "Game Over! Word was: ${gameState.word.value}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameState.isWon) Color.Green else Color.Red
            )
        }

        Spacer(Modifier.height(16.dp))

        // New Game Button
        Button(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = Color.Black
            )
        ) {
            Text("New Game")
        }
    }
}

/**
 * Displays the hangman image for current game state
 */
@Composable
private fun HangmanImage(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier.size(200.dp))
}
// endregion

// region Input Components
/**
 * Grid layout for letter selection buttons
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LetterGrid(
    gameState: GameState,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ('A'..'Z').forEach { letter ->
            LetterButton(
                letter = letter,
                onClick = { onLetterClick(letter) },
                isEnabled = letter !in gameState.guessedLetters && !gameState.isGameOver
            )
        }
    }
}

/**
 * Individual letter button with state-dependent styling
 */
@Composable
private fun LetterButton(
    letter: Char,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(0.dp)
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color.LightGray else Color.Gray,
            contentColor = if (isEnabled) Color.Black else Color.DarkGray
        ),
        enabled = isEnabled
    ) {
        Text(
            text = letter.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Hint controls section (only shown in landscape mode)
 */
@Composable
private fun HintSection(
    gameState: GameState,
    onHintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Display hint text if available
        if (gameState.hintCount >= 1) {
            Text(
                text = "Hint: ${gameState.word.hint}",
                modifier = Modifier.padding(8.dp),
                fontSize = 16.sp
            )
        }

        // Hint Button
        Button(
            onClick = onHintClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            enabled = gameState.hintCount < 3 && !gameState.isGameOver
        ) {
            Text("Hint (${3 - gameState.hintCount} remaining)")
        }
    }
}

// endregion

// region Hint Logic
/**
 * Handles hint system logic with state updates
 */
private fun handleHintRequest(
    gameState: GameState,
    updateState: (GameState) -> Unit,
    showSnackbar: (String) -> Unit
) {
    if (gameState.isGameOver) return

    when (gameState.hintCount) {
        0 -> updateState(gameState.copy(hintCount = 1))
        1 -> handleFirstHint(gameState, updateState, showSnackbar)
        2 -> handleSecondHint(gameState, updateState, showSnackbar)
    }
}

private fun handleFirstHint(
    gameState: GameState,
    updateState: (GameState) -> Unit,
    showSnackbar: (String) -> Unit
) {
    if (gameState.incorrectGuesses >= 5) {
        showSnackbar("Hint unavailable - too many incorrect guesses")
        return
    }
    val remainingLetters = ('A'..'Z') - gameState.guessedLetters - gameState.word.value.toSet()
    updateState(gameState.copy(
        guessedLetters = gameState.guessedLetters + remainingLetters.shuffled().take(remainingLetters.size / 2),
        incorrectGuesses = gameState.incorrectGuesses + 1,
        hintCount = 2
    ))
}

private fun handleSecondHint(
    gameState: GameState,
    updateState: (GameState) -> Unit,
    showSnackbar: (String) -> Unit
) {
    if (gameState.incorrectGuesses >= 5) {
        showSnackbar("Hint unavailable - too many incorrect guesses")
        return
    }
    updateState(gameState.copy(
        guessedLetters = gameState.guessedLetters + setOf('A', 'E', 'I', 'O', 'U'),
        incorrectGuesses = gameState.incorrectGuesses + 1,
        hintCount = 3
    ))
}
// endregion

@Preview(showBackground = true)
@Composable
fun HangmanGamePreview() {
    HangmanTheme {
        HangmanGameScreen()
    }
}