package com.app.velopath.destinations

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun PrintHome(modifier: Modifier = Modifier, navButton: @Composable () -> Unit) {
    val context = LocalContext.current

    // Define initial position for the map (e.g., San Francisco)
    val initialPosition = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }

    // Request location permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                // Handle permission denial (optional)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Main UI structure
    Column(modifier = modifier.fillMaxSize()) {
        // Navigation button at the top
        navButton()

        // Map occupies 3/4 of the available space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            )
        }

        // Remaining space for other content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hello Home",
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}