package com.example.locationinformationapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import java.io.IOException
import java.util.Locale

/**
 * Main activity for the Location Information Application.
 * Handles location permissions, initializes location services, and sets up the main UI.
 */
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MapScreen(fusedLocationClient)
                }
            }
        }
    }
}

/**
 * Data class representing a customizable marker
 */
data class CustomMarker(
    val id: String,
    val position: LatLng,
    val title: String = "",
    val color: Float = BitmapDescriptorFactory.HUE_RED,
    val isDraggable: Boolean = true,
    val alpha: Float = 1.0f
)

/**
 * Main composable function for the map screen.
 * Manages location data, markers, and user interactions with the map.
 */
@Composable
fun MapScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var address by remember { mutableStateOf("Tap on the map or a marker") }
    val markers = remember { mutableStateListOf<CustomMarker>() }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    // State for dialogs
    var showActionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var selectedMarker by remember { mutableStateOf<CustomMarker?>(null) }
    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }

    // Color options for the color picker dialog
    val colorOptions = listOf(
        Pair("Red", BitmapDescriptorFactory.HUE_RED),
        Pair("Blue", BitmapDescriptorFactory.HUE_BLUE),
        Pair("Green", BitmapDescriptorFactory.HUE_GREEN),
        Pair("Yellow", BitmapDescriptorFactory.HUE_YELLOW),
        Pair("Orange", BitmapDescriptorFactory.HUE_ORANGE)
    )

    // Action Dialog (Change Color/Delete)
    if (showActionDialog && selectedMarker != null) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Marker: ${selectedMarker?.title}") },
            text = {
                Column {
                    Text(address, modifier = Modifier.padding(bottom = 16.dp))
                    Button(
                        onClick = {
                            showColorDialog = true
                            showActionDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Color")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showDeleteDialog = true
                            showActionDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Marker")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showActionDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedMarker != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Marker") },
            text = { Text("Are you sure you want to delete this marker?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        markers.remove(selectedMarker)
                        selectedMarker = null
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Color Picker Dialog
    if (showColorDialog && selectedMarker != null) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Change Marker Color") },
            text = {
                Column {
                    colorOptions.forEach { (colorName, hueValue) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    markers.replaceAll { marker ->
                                        if (marker.id == selectedMarker?.id) {
                                            marker.copy(color = hueValue)
                                        } else {
                                            marker
                                        }
                                    }
                                    showColorDialog = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        when (hueValue) {
                                            BitmapDescriptorFactory.HUE_RED -> Color.Red
                                            BitmapDescriptorFactory.HUE_BLUE -> Color.Blue
                                            BitmapDescriptorFactory.HUE_GREEN -> Color.Green
                                            BitmapDescriptorFactory.HUE_YELLOW -> Color.Yellow
                                            BitmapDescriptorFactory.HUE_ORANGE -> Color(0xFFFFA500)
                                            else -> Color.Gray
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = colorName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showColorDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context, fusedLocationClient) { location ->
                currentLocation = location
                location?.let {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(it, 15f)
                            )
                        )
                        getAddress(context, it) { addr -> address = addr }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(context, fusedLocationClient) { location ->
                currentLocation = location
                location?.let {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(it, 15f)
                            )
                        )
                        getAddress(context, it) { addr -> address = addr }
                    }
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            // Regular click shows address
            onMapClick = { latLng ->
                selectedPosition = latLng
                selectedMarker = null
                getAddress(context, latLng) { addr ->
                    address = "Location:\n$addr"
                }
            },
            // Long click adds marker
            onMapLongClick = { latLng ->
                val newMarker = CustomMarker(
                    id = System.currentTimeMillis().toString(),
                    position = latLng,
                    title = "Marker ${markers.size + 1}",
                    color = colorOptions.random().second,
                    isDraggable = true
                )
                markers.add(newMarker)
                getAddress(context, latLng) { addr ->
                    address = "Added marker:\n${newMarker.title}\n$addr"
                }
            }
        ) {

            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Your Location",
                    snippet = address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    draggable = false,
                    onClick = {
                        getAddress(context, it.position) { addr ->
                            address = "Your Location:\n$addr"
                        }
                        true
                    }
                )
            }

            markers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                    snippet = address, // Shows address in info window
                    draggable = marker.isDraggable,
                    icon = BitmapDescriptorFactory.defaultMarker(marker.color),
                    alpha = marker.alpha,
                    onClick = {
                        selectedMarker = marker
                        getAddress(context, marker.position) { addr ->
                            address = "${marker.title}\n$addr"
                        }
                        showActionDialog = true
                        true
                    }
                )
            }
        }

        // Address display container at bottom center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                color = Color(0xFFFDF5E6),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = address,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Retrieves the device's last known location.
 */
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    callback: (LatLng?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                callback(LatLng(it.latitude, it.longitude))
            } ?: run {
                Log.e("Location", "Unable to get current location")
                callback(null)
            }
        }
    }
}

/**
 * Converts geographic coordinates to a human-readable address using Geocoder.
 */
private fun getAddress(
    context: Context,
    latLng: LatLng,
    callback: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        val geocoder = Geocoder(context, Locale.getDefault())
        val latitude = latLng.latitude
        val longitude = latLng.longitude
        val maxResults = 1

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, maxResults)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses.first()
                val addressText = address.getAddressLine(0) ?: "Unknown address"
                withContext(Dispatchers.Main) {
                    callback(addressText)
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback("No address found")
                }
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Error getting address: ${e.message}")
            withContext(Dispatchers.Main) {
                callback("Address unavailable")
            }
        } catch (e: IllegalArgumentException) {
            Log.e("Geocoder", "Invalid latitude or longitude: ${e.message}")
            withContext(Dispatchers.Main) {
                callback("Invalid location")
            }
        }
    }
}