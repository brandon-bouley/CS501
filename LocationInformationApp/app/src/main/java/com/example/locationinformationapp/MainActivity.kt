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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import android.location.Address
import android.location.Geocoder.GeocodeListener
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.IOException
import java.util.Locale

/**
 * Main activity for the Location Information Application.
 * Handles location permissions, initializes location services, and sets up the main UI.
 */
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Called when the activity is starting. Initializes location services and sets up the UI.
     * @param savedInstanceState If the activity is being re-initialized, this contains data from onSaveInstanceState.
     */
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
 * Main composable function for the map screen.
 * Manages location data, markers, and user interactions with the map.
 * @param fusedLocationClient The FusedLocationProviderClient for location services
 */
@Composable
fun MapScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var address by remember { mutableStateOf("Fetching address...") }
    val markers = remember { mutableStateListOf<LatLng>() }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    // Permission handling
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
            // Already have permission - get location
            getCurrentLocation(context, fusedLocationClient) { location ->
                currentLocation = location
                location?.let {
                    coroutineScope.launch {
                        // Animate the camera position
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(it, 15f)
                            )
                        )
                        // Get the address after the animation
                        getAddress(context, it) { addr -> address = addr }
                    }
                }
            }
        } else {
            // Request location permission
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            onMapClick = { latLng ->
                markers.add(latLng)
                getAddress(context, latLng) { addr -> address = addr }
            }
        ) {
            // Current location marker
            currentLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Your Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Custom markers
            markers.forEach { position ->
                Marker(
                    state = MarkerState(position = position),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }

        // Address display container at bottom center
        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
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
 * @param context The application context
 * @param fusedLocationClient The FusedLocationProviderClient instance
 * @param callback Function to handle the location result (LatLng? -> Unit)
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
        // Get last known location using Fused Location Provider
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
 * Handles different Android versions and potential errors.
 * @param context The application context
 * @param latLng The geographic coordinates to convert
 * @param callback Function to handle the address result (String -> Unit)
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

        // GeocodeListener implementation for API 33+
        val geocodeListener = @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        object : GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val address = addresses.first()
                    val addressText = address.getAddressLine(0) ?: "Unknown address"
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(addressText)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        callback("No address found")
                    }
                }
            }

            override fun onError(errorMessage: String?) {
                Log.e("Geocoder", "Error getting address: $errorMessage")
                CoroutineScope(Dispatchers.Main).launch {
                    callback("Address unavailable")
                }
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use asynchronous geocoding for newer Android versions
                geocoder.getFromLocation(latitude, longitude, maxResults, geocodeListener)
            } else {
                // Synchronous fallback for older versions
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