package com.example.ballgame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * MainActivity - Entry point for the BallGame app.
 * Uses gyroscope sensor data to move a ball through a maze.
 */
class MainActivity : ComponentActivity(), SensorEventListener {
    private var gyroX by mutableFloatStateOf(0f)
    private var gyroY by mutableFloatStateOf(0f)

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        setContent { BallGame(gyroX, gyroY) }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroX = it.values[0]
                gyroY = it.values[1]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
/**
 * BallGame - Composable function that renders the game UI and handles physics.
 */
    @Composable
    fun BallGame(gyroX: Float, gyroY: Float) {
    var ballPosition by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var gameWon by remember { mutableStateOf(false) }

    val ballRadius = 20f
    val gridSize by remember(canvasSize) {
        mutableFloatStateOf(
            if (canvasSize == Size.Zero) 50f
            else minOf(canvasSize.width / 11, canvasSize.height / 24)
        )
    }

    val mazeGrid = listOf(
        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        listOf(1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1),
        listOf(1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1),
        listOf(1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1),
        listOf(1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1),
        listOf(1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1),
        listOf(1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1),
        listOf(1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 2, 1),
        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )

    val walls = remember { mutableStateListOf<Rect>() }
    var exitRect by remember { mutableStateOf<Rect?>(null) }
    var velocity by remember { mutableStateOf(Offset.Zero) }
    val maxSpeed = 300f

    LaunchedEffect(gridSize) {
        walls.clear()
        for (row in mazeGrid.indices) {
            for (col in mazeGrid[row].indices) {
                val x = col * gridSize
                val y = row * gridSize
                when (mazeGrid[row][col]) {
                    1 -> walls.add(Rect(x, y, x + gridSize, y + gridSize))
                    2 -> exitRect = Rect(x, y, x + gridSize, y + gridSize)
                }
            }
        }
        ballPosition = Offset(gridSize * 1.5f, gridSize * 1.5f)
    }

    LaunchedEffect(gyroX, gyroY) {
        while (!gameWon) {
            val accel = Offset(gyroY * 50f, gyroX * 50f)
            velocity = Offset(
                (velocity.x + accel.x).coerceIn(-maxSpeed, maxSpeed),
                (velocity.y + accel.y).coerceIn(-maxSpeed, maxSpeed)
            )

            var tempPosition = ballPosition

            // Handle X-axis movement first
            val nextXPosition = tempPosition + Offset(velocity.x * 0.016f, 0f)
            val ballRectX = Rect(
                nextXPosition.x - ballRadius, tempPosition.y - ballRadius,
                nextXPosition.x + ballRadius, tempPosition.y + ballRadius
            )

            var newVelocityX = velocity.x
            var newX = nextXPosition.x

            for (wall in walls) {
                if (ballRectX.overlaps(wall)) {
                    if (velocity.x > 0) newX = wall.left - ballRadius
                    if (velocity.x < 0) newX = wall.right + ballRadius
                    newVelocityX = 0f
                    break // Stop checking after first collision
                }
            }

            tempPosition = tempPosition.copy(x = newX)
            velocity = velocity.copy(x = newVelocityX)

            // Handle Y-axis movement separately
            val nextYPosition = tempPosition + Offset(0f, velocity.y * 0.016f)
            val ballRectY = Rect(
                tempPosition.x - ballRadius, nextYPosition.y - ballRadius,
                tempPosition.x + ballRadius, nextYPosition.y + ballRadius
            )

            var newVelocityY = velocity.y
            var newY = nextYPosition.y

            for (wall in walls) {
                if (ballRectY.overlaps(wall)) {
                    if (velocity.y > 0) newY = wall.top - ballRadius
                    if (velocity.y < 0) newY = wall.bottom + ballRadius
                    newVelocityY = 0f
                    break // Stop checking after first collision
                }
            }

            tempPosition = tempPosition.copy(y = newY)
            velocity = velocity.copy(y = newVelocityY)

            // Keep the ball within bounds
            tempPosition = Offset(
                tempPosition.x.coerceIn(ballRadius, canvasSize.width - ballRadius),
                tempPosition.y.coerceIn(ballRadius, canvasSize.height - ballRadius)
            )

            ballPosition = tempPosition

            // Check if the ball reaches the exit
            exitRect?.let { if (Rect(ballPosition, ballRadius * 2).overlaps(it)) gameWon = true }

            delay(16)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (!gameWon) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .onSizeChanged {
                        canvasSize = Size(it.width.toFloat(), it.height.toFloat())
                    }
            ) {
                // Calculate centering offset
                val mazeWidth = 11 * gridSize
                val mazeHeight = 24 * gridSize
                val offsetX = (size.width - mazeWidth) / 2
                val offsetY = (size.height - mazeHeight) / 2

                // Apply offset to all drawing operations
                translate(left = offsetX, top = offsetY) {
                    walls.forEach { wall ->
                        drawRect(
                            Color.Blue,
                            topLeft = Offset(wall.left, wall.top),
                            size = Size(wall.width, wall.height)
                        )
                    }
                    exitRect?.let {
                        drawRect(
                            Color.Yellow,
                            topLeft = Offset(it.left, it.top),
                            size = Size(it.width, it.height)
                        )
                    }
                    drawCircle(Color.Red, ballRadius, ballPosition)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(50.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("You Win!", color = Color.Yellow, fontSize = 40.sp)
                Button(onClick = {
                    gameWon = false; ballPosition = Offset(gridSize * 1.5f, gridSize * 1.5f)
                }) {
                    Text("Play Again")
                }
            }
        }
    }
}

