package com.example.soundmeter

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.soundmeter.ui.theme.SoundMeterTheme
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private var audioRecord: AudioRecord? = null // AudioRecord object for capturing audio
    private var isRecording by mutableStateOf(false) // State to track if recording is active
    private var recordingThread: Thread? = null // Thread for processing audio data
    private val bufferSize by lazy { // Buffer size for audio recording
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
    }

    // Sound measurement states
    private var decibel by mutableFloatStateOf(0f) // Current sound level in decibels
    private var isLoud by mutableStateOf(false) // State to track if sound exceeds the danger threshold
    private var baselineNoise = 0.0 // Baseline noise level for calibration

    // Permission launcher for requesting microphone access
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) toggleRecording() // Start recording if permission is granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoundMeterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoundMeterScreen(
                        decibel = decibel,
                        isLoud = isLoud,
                        isRecording = isRecording,
                        onToggleRecording = { checkPermissionAndToggle() }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) stopRecording() // Stop recording if the app is paused
    }

    /**
     * Checks if the RECORD_AUDIO permission is granted.
     * If granted, toggles recording; otherwise, requests permission.
     */
    private fun checkPermissionAndToggle() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            toggleRecording()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Toggles the recording state.
     * Starts recording if not already recording; otherwise, stops recording.
     */
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
        isRecording = !isRecording
    }

    /**
     * Initializes and starts audio recording.
     * Sets up the AudioRecord object, starts recording, and begins processing audio data.
     */
    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        calibrateBaselineNoise() // Calibrate baseline noise level

        // Start a thread to process audio data
        recordingThread = Thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read > 0) {
                    processAudioBuffer(buffer, read)
                }
            }
        }.apply { start() }
    }

    /**
     * Stops audio recording and releases resources.
     */
    private fun stopRecording() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread?.interrupt()
        recordingThread = null
    }

    /**
     * Calibrates the baseline noise level by measuring the RMS of a silent buffer.
     */
    private fun calibrateBaselineNoise() {
        val buffer = ShortArray(bufferSize)
        val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
        if (read > 0) {
            val rms = calculateRMS(buffer, read)
            baselineNoise = 20.0 * log10(rms / 32767.0) + 50 // Normalized baseline
        }
    }

    /**
     * Processes the audio buffer to calculate the current sound level in decibels.
     * @param buffer The audio buffer containing raw PCM data.
     * @param bytesRead The number of bytes read from the buffer.
     */
    private fun processAudioBuffer(buffer: ShortArray, bytesRead: Int) {
        val rms = calculateRMS(buffer, bytesRead)
        if (rms == 0.0) {
            decibel = 0f
            return
        }

        val db = 20.0 * log10(rms / 32767.0) // Convert RMS to decibels
        val normalizedDb = (db + 20 + baselineNoise).coerceAtLeast(0.0) // Normalize and clamp to 0

        decibel = normalizedDb.toFloat()
        isLoud = decibel > DANGER_THRESHOLD // Check if sound exceeds the danger threshold
    }

    /**
     * Calculates the Root Mean Square (RMS) of the audio buffer.
     * @param buffer The audio buffer containing raw PCM data.
     * @param bytesRead The number of bytes read from the buffer.
     * @return The RMS value of the buffer.
     */
    private fun calculateRMS(buffer: ShortArray, bytesRead: Int): Double {
        var sum = 0.0
        for (i in 0 until bytesRead) {
            sum += buffer[i].toDouble().pow(2)
        }
        return sqrt(sum / bytesRead)
    }

    companion object {
        const val SAMPLE_RATE = 44100 // Audio sample rate in Hz
        const val DANGER_THRESHOLD = 80f // Threshold for loud noise warning
    }
}

/**
 * Composable function for the main sound meter screen.
 * Displays the current sound level, a visual meter, and a start/stop button.
 * @param decibel The current sound level in decibels.
 * @param isLoud Whether the sound level exceeds the danger threshold.
 * @param isRecording Whether the app is currently recording audio.
 * @param onToggleRecording Callback to toggle recording state.
 */
@Composable
fun SoundMeterScreen(
    decibel: Float,
    isLoud: Boolean,
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // dB value display
        Text(
            text = "%.1f dB".format(decibel),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sound level meter
        SoundLevelMeter(decibel = decibel, isLoud = isLoud)

        Spacer(modifier = Modifier.height(32.dp))

        // Start/Stop button
        Button(
            onClick = onToggleRecording,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isRecording) "Stop Measuring" else "Start Measuring")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning message
        if (isLoud) {
            Text(
                text = "Warning: Loud noise detected!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Composable function for the sound level meter.
 * Displays a visual representation of the current sound level.
 * @param decibel The current sound level in decibels.
 * @param isLoud Whether the sound level exceeds the danger threshold.
 */
@Composable
fun SoundLevelMeter(decibel: Float, isLoud: Boolean) {
    val animatedLevel by animateFloatAsState(
        targetValue = decibel.coerceIn(0f, 120f), // Clamp between 0-120 dB
        label = "soundLevelAnimation"
    )

    Box(modifier = Modifier.size(200.dp, 400.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw meter background
            drawRect(
                color = Color.LightGray,
                size = Size(size.width, size.height)
            )

            // Draw current level
            val levelHeight = size.height * (animatedLevel / 120)
            drawRect(
                color = if (isLoud) Color.Red else Color.Green,
                topLeft = Offset(0f, size.height - levelHeight),
                size = Size(size.width, levelHeight)
            )

            // Draw threshold marker
            val thresholdHeight = size.height * (MainActivity.DANGER_THRESHOLD / 120)
            drawLine(
                color = Color.Black,
                start = Offset(0f, size.height - thresholdHeight),
                end = Offset(size.width, size.height - thresholdHeight),
                strokeWidth = 4f
            )
        }
    }
}