package com.example.compassapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compassapp.ui.theme.CompassAppTheme
import java.lang.Math.toDegrees
import kotlin.math.*

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Sensor data storage
    private val gravity = FloatArray(3) // Stores accelerometer data (gravity vector)
    private val geomagnetic = FloatArray(3) // Stores magnetometer data (geomagnetic vector)
    private val rotationMatrix = FloatArray(9) // Used to calculate device orientation
    private val orientation = FloatArray(3) // Stores azimuth, pitch, and roll

    private var lastUpdateTime = System.nanoTime()
    private var pitch by mutableFloatStateOf(0f) // Device pitch (tilt forward/backward)
    private var roll by mutableFloatStateOf(0f) // Device roll (tilt left/right)
    private var azimuth by mutableFloatStateOf(0f) // Device azimuth (compass heading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager and sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Set up the Compose UI
        setContent {
            CompassAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompassScreen(azimuth = azimuth, pitch = pitch, roll = roll)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listeners when the app is in the foreground
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to save battery when the app is in the background
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.nanoTime()
        val dt = (currentTime - lastUpdateTime) / 1_000_000_000.0  // Time difference in seconds
        lastUpdateTime = currentTime

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, gravity, 0, 3)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            Sensor.TYPE_GYROSCOPE -> {
                // Calculate pitch and roll using gyroscope data (angular velocity)
                val gyroPitchDelta = event.values[1] * dt * (180 / Math.PI).toFloat()
                val gyroRollDelta = event.values[2] * dt * (180 / Math.PI).toFloat()

                // Calculate pitch and roll using accelerometer data (gravity vector)
                val pitchAccel = toDegrees(atan2(-gravity[0], sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])).toDouble())
                val rollAccel = toDegrees(atan2(gravity[1], gravity[2]).toDouble())

                // Combine gyroscope and accelerometer data using a complementary filter
                val alpha = 0.98f // Weight for gyroscope data
                pitch = (alpha * (pitch + gyroPitchDelta) + (1 - alpha) * pitchAccel).toFloat()
                roll = (alpha * (roll + gyroRollDelta) + (1 - alpha) * rollAccel).toFloat()
            }
        }

        // Calculate azimuth (compass heading) using accelerometer and magnetometer data
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDegrees = toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuthDegrees + 360) % 360 // Normalize to 0-360 degrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Composable function for the main compass screen.
 * Displays the compass needle, pitch, roll, and a direction message.
 */
@Composable
fun CompassScreen(azimuth: Float, pitch: Float, roll: Float) {

    val backgroundColor = lerp(Color(0xFF87CEEB), Color(0xFF001F3F), (1 - abs(azimuth - 180) / 180))
    val directionMessage = getDirectionMessage(azimuth)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(directionMessage, color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            CompassNeedle(heading = azimuth)
            Spacer(modifier = Modifier.height(32.dp))
            LevelIndicator(label = "Pitch", value = pitch)
            Spacer(modifier = Modifier.height(16.dp))
            LevelIndicator(label = "Roll", value = roll)
        }
    }
}

/**
 * Returns a direction message based on the azimuth angle.
 * @param azimuth The compass heading in degrees (0-360).
 */
fun getDirectionMessage(azimuth: Float): String {
    return when (azimuth) {
        in 337.5f..360f, in 0f..22.5f -> "Heading North!"
        in 22.5f..67.5f -> "Heading Northeast!"
        in 67.5f..112.5f -> "Heading East!"
        in 112.5f..157.5f -> "Heading Southeast!"
        in 157.5f..202.5f -> "Heading South!"
        in 202.5f..247.5f -> "Heading Southwest!"
        in 247.5f..292.5f -> "Heading West!"
        in 292.5f..337.5f -> "Heading Northwest!"
        else -> "Heading... somewhere?"
    }
}

/**
 * Composable function for the compass needle.
 * Animates the rotation of the needle based on the heading.
 * @param heading The compass heading in degrees (0-360).
 */
@Composable
fun CompassNeedle(heading: Float) {
    var prevHeading by remember { mutableFloatStateOf(heading) }
    var displayHeading by remember { mutableFloatStateOf(heading) }

    LaunchedEffect(heading) {
        val delta = ((heading - prevHeading + 540) % 360) - 180  // Shortest rotation path
        displayHeading += delta
        prevHeading = heading
    }

    val animatedHeading by animateFloatAsState(
        targetValue = -displayHeading,  // Apply smooth animation
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compassRotation"
    )

    Image(
        painter = painterResource(R.drawable.compass),
        contentDescription = "Compass",
        modifier = Modifier
            .size(300.dp)
            .rotate(animatedHeading),
        contentScale = ContentScale.Fit
    )
}

/**
 * Composable function for displaying pitch or roll values.
 * @param label The label to display (e.g., "Pitch" or "Roll").
 * @param value The value to display in degrees.
 */
@Composable
fun LevelIndicator(label: String, value: Float) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(40.dp)
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Text(
            text = "$label: ${"%.1f°".format(value)}",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}