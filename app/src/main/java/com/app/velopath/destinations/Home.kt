package com.app.velopath.destinations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.app.velopath.mapsHandling.GrantPermissionAndMove
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
    val initialPosition = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }

    GrantPermissionAndMove(cameraPositionState = cameraPositionState, context = context)

    Column(modifier = modifier.fillMaxSize()) {
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                navButton()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hello Home",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}