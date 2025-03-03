package com.example.altitutdeapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import androidx.compose.ui.tooling.preview.Preview
import com.example.altitutdeapp.ui.theme.AltitutdeAppTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null
    private var currentPressure by mutableFloatStateOf(1013.25f)
    private var currentAltitude by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        setContent {
            AltitutdeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AltimeterScreen(
                        altitude = currentAltitude,
                        pressure = currentPressure
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.firstOrNull()?.let {
            currentPressure = it
            currentAltitude = calculateAltitude(currentPressure)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calculateAltitude(pressure: Float): Float {
        val p0 = 1013.25
        val p = pressure.toDouble()
        return (44330 * (1 - (p / p0).pow(1 / 5.255))).toFloat()
    }
}

@Composable
fun AltimeterScreen(altitude: Float, pressure: Float) {
    val backgroundColor = calculateBackgroundColor(altitude)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Altitude: ${"%.2f".format(altitude)} meters",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pressure: ${"%.1f".format(pressure)} hPa",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun calculateBackgroundColor(altitude: Float): Color {
    val maxAltitude = 44330f
    val fraction = (altitude / maxAltitude).coerceIn(0f, 1f)
    return lerp(Color.Cyan, Color.DarkGray, fraction)
}

@Preview(showBackground = true)
@Composable
fun AltimeterPreview() {
    AltitutdeAppTheme {
        AltimeterScreen(
            altitude = 1500f,
            pressure = 850f
        )
    }
}