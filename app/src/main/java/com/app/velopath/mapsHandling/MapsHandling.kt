package com.app.velopath.mapsHandling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun GrantPermissionAndMove(cameraPositionState: CameraPositionState, context: Context) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                moveToUserLocation(cameraPositionState, context)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (isLocationPermissionGranted(context)) {
            moveToUserLocation(cameraPositionState, context)
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

fun moveToUserLocation(cameraPositionState: CameraPositionState, context: Context) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
            }
        }
    }
}