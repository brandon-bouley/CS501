package com.example.polylineapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.PolyUtil

/**
 * Main activity for the Polyline App that displays a map with customizable trails and forest boundaries.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. Sets up the content view with the MapScreen composable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MapScreen()
            }
        }
    }
}

/**
 * Main composable that displays the Google Map with interactive elements.
 * Includes:
 * - Forest boundary polygon with customizable appearance
 * - Trail polyline with customizable appearance
 * - Control panel for adjusting visual properties
 * - Information dialog for location details
 */
@Composable
fun MapScreen() {
    // Camera state for controlling map position and zoom
    val cameraPositionState = rememberCameraPositionState()

    // Center point for the forest area
    val forestCenter = remember { LatLng(42.57315138723262, -71.30069032799875) }

    // Encoded polyline string representing the trail path
    val encodedPolyline = remember { "_zybGb|crLmKnr@kDqCiBpCwBwFbDuAv@qNpAyCcAmE}Ck@kBlL[jDeB`@e@sDXyFwCH?`Gk@lDcFm_@nj@Y" }

    // State variables for polyline appearance
    var polylineColor by remember { mutableStateOf(Color.Blue) }
    var polylineWidth by remember { mutableFloatStateOf(8f) }

    // State for displaying overlay information
    var selectedOverlayInfo by remember { mutableStateOf<String?>(null) }

    // State variables for polygon appearance
    var polygonFillColor by remember { mutableStateOf(Color(0x3300FF00)) }
    var polygonStrokeColor by remember { mutableStateOf(Color.Green) }
    var polygonStrokeWidth by remember { mutableFloatStateOf(5f) }

    /**
     * Decodes the encoded polyline string into a list of LatLng points.
     * Falls back to forest center if decoding fails.
     */
    val trailPath = remember {
        try {
            PolyUtil.decode(encodedPolyline)
        } catch (e: Exception) {
            Log.e("Polyline", "Error decoding polyline: ${e.message}")
            listOf(forestCenter) // Fallback to single point
        }
    }

    /**
     * Defines the boundary coordinates for the forest area polygon.
     */
    val forestBoundary = remember {
        listOf(
            LatLng(42.56363, -71.29735),
            LatLng(42.56337, -71.29924),
            LatLng(42.57564, -71.30507),
            LatLng(42.58303, -71.29692),
            LatLng(42.58234, -71.29126),
            LatLng(42.57539, -71.29024),
            LatLng(42.57437, -71.29162),
            LatLng(42.57465, -71.29316),
            LatLng(42.57288, -71.29323),
            LatLng(42.57072, -71.29617),
            LatLng(42.56543, -71.29771),
            LatLng(42.56353, -71.29732)
        )
    }

    // Animates the camera to focus on the forest center when first launched
    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(forestCenter, 13f)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.HYBRID
            )
        ) {
            // Forest boundary polygon with customizable properties
            Polygon(
                points = forestBoundary,
                fillColor = polygonFillColor,
                strokeColor = polygonStrokeColor,
                strokeWidth = polygonStrokeWidth,
                clickable = true,
                onClick = {

                    selectedOverlayInfo = "Billerica State Forest\nArea: 370 acres\nOpen: 7AM - 7PM"
                }
            )

            // Trail polyline with customizable properties
            Polyline(
                points = trailPath,
                color = polylineColor,
                width = polylineWidth,
                clickable = true,
                onClick = {
                    selectedOverlayInfo = "Billerica State Forest Trail\nLength: 1.2 miles\nDifficulty: Easy"
                }
            )
        }


        ControlsPanel(
            polylineColor = polylineColor,
            polylineWidth = polylineWidth,
            polygonFillColor = polygonFillColor,
            polygonStrokeColor = polygonStrokeColor,
            polygonStrokeWidth = polygonStrokeWidth,
            onPolylineColorChange = { polylineColor = it },
            onPolylineWidthChange = { polylineWidth = it },
            onPolygonFillColorChange = { polygonFillColor = it },
            onPolygonStrokeColorChange = { polygonStrokeColor = it },
            onPolygonStrokeWidthChange = { polygonStrokeWidth = it },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 56.dp)
        )
    }

    // Information dialog that shows when an overlay is clicked
    selectedOverlayInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { selectedOverlayInfo = null },
            title = { Text("Location Info") },
            text = { Text(info) },
            confirmButton = {
                Button(onClick = { selectedOverlayInfo = null }) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * Composable that provides controls for adjusting map element appearances.
 *
 * @param polylineColor Current color of the trail polyline
 * @param polylineWidth Current width of the trail polyline
 * @param polygonFillColor Current fill color of the forest polygon
 * @param polygonStrokeColor Current border color of the forest polygon
 * @param polygonStrokeWidth Current border width of the forest polygon
 * @param onPolylineColorChange Callback for polyline color changes
 * @param onPolylineWidthChange Callback for polyline width changes
 * @param onPolygonFillColorChange Callback for polygon fill color changes
 * @param onPolygonStrokeColorChange Callback for polygon border color changes
 * @param onPolygonStrokeWidthChange Callback for polygon border width changes
 * @param modifier Modifier for styling/layout
 */
@Composable
private fun ControlsPanel(
    polylineColor: Color,
    polylineWidth: Float,
    polygonFillColor: Color,
    polygonStrokeColor: Color,
    polygonStrokeWidth: Float,
    onPolylineColorChange: (Color) -> Unit,
    onPolylineWidthChange: (Float) -> Unit,
    onPolygonFillColorChange: (Color) -> Unit,
    onPolygonStrokeColorChange: (Color) -> Unit,
    onPolygonStrokeWidthChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Toggle state for showing polygon vs polyline controls
    var showPolygonControls by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = 0.dp,
                bottomStart = 16.dp
            ),
            modifier = Modifier
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.66f)
                .padding(end = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toggle button for switching between trail and forest controls
                Button(
                    onClick = { showPolygonControls = !showPolygonControls },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        if (showPolygonControls) "Trail Settings" else "Forest Settings",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (showPolygonControls) {
                    // Forest (polygon) controls section
                    Text(
                        "FOREST SETTINGS",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SettingItem("Fill Color") {
                            ColorSlider(
                                initialColor = polygonFillColor,
                                onColorSelected = {
                                    onPolygonFillColorChange(it.copy(alpha = 0.3f))
                                }
                            )
                        }

                        SettingItem("Border Color") {
                            ColorSlider(
                                initialColor = polygonStrokeColor,
                                onColorSelected = onPolygonStrokeColorChange
                            )
                        }

                        SettingItem("Border Width") {
                            Slider(
                                value = polygonStrokeWidth,
                                onValueChange = onPolygonStrokeWidthChange,
                                valueRange = 4f..20f,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    // Trail (polyline) controls section
                    Text(
                        "TRAIL SETTINGS",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SettingItem("Trail Color") {
                            ColorSlider(
                                initialColor = polylineColor,
                                onColorSelected = onPolylineColorChange
                            )
                        }

                        SettingItem("Trail Width") {
                            Slider(
                                value = polylineWidth,
                                onValueChange = onPolylineWidthChange,
                                valueRange = 4f..20f,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable that creates a labeled setting item with content.
 *
 * @param label Text label for the setting
 * @param content Composable content to display below the label
 */
@Composable
private fun SettingItem(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

/**
 * Composable that displays a hue slider for color selection.
 *
 * @param initialColor Initial color value
 * @param onColorSelected Callback when color is changed
 */
@Composable
fun ColorSlider(initialColor: Color, onColorSelected: (Color) -> Unit) {
    // Convert color to HSV to extract hue value
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    var hue by remember { mutableFloatStateOf(hsv[0]) }

    Column {
        Slider(
            value = hue,
            onValueChange = {
                hue = it
                onColorSelected(Color.hsv(hue, 1f, 1f))
            },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color.hsv(hue, 1f, 1f),
                activeTrackColor = Color.hsv(hue, 1f, 1f)
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}