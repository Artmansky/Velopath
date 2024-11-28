package com.app.velopath.mapsHandling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.app.velopath.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

class MapsHandling(private val context: Context) {
    private var defaultLocation: Pair<Double, Double> = Pair(-73.977001, 40.728847)

    @Composable
    fun PrintMap(modifier: Modifier, darkMode: Boolean, onClick: () -> Unit) {
        var hasPermission by remember { mutableStateOf(isLocationPermissionGranted(context)) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted -> hasPermission = isGranted }
        )

        LaunchedEffect(hasPermission) {
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        ShowMap(modifier, darkMode, hasPermission, onClick)
    }

    @Composable
    private fun ShowMap(
        modifier: Modifier,
        darkMode: Boolean,
        hasPermission: Boolean,
        onClick: () -> Unit
    ) {
        val userLocation = remember {
            if (hasPermission) getCurrentLocation(context) else null
        } ?: defaultLocation

        val cameraPositionState = remember {
            CameraPositionState(
                CameraPosition(
                    LatLng(userLocation.first, userLocation.second),
                    15f,
                    0f,
                    0f
                )
            )
        }
        val mapStyleOptions = remember(darkMode) {
            if (darkMode) {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map)
            } else {
                null
            }
        }

        val isMarkingEnabled = remember {
            mutableStateOf(false)
        }
        val mapProperties =
            remember {
                mutableStateOf(
                    MapProperties(
                        isMyLocationEnabled = hasPermission,
                        mapStyleOptions = mapStyleOptions
                    )
                )
            }
        val uiSettings =
            remember { MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false) }
        val markers = remember { mutableStateListOf<LatLng>() }

        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.80f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = mapProperties.value,
                    uiSettings = uiSettings,
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        if (isMarkingEnabled.value) {
                            markers.add(latLng)
                            isMarkingEnabled.value = false
                        }
                    }
                ) {
                    markers.forEach { latLng ->
                        Marker(
                            state = MarkerState(position = latLng)
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .align(Alignment.TopStart)
                ) {
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        FloatingActionButton(
                            onClick = onClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = modifier.size(32.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (hasPermission) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition(
                                            LatLng(
                                                userLocation.first,
                                                userLocation.second
                                            ), 15f, 0f, 0f
                                        )
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "User's location unknown",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Follow Location",
                            modifier = modifier.size(32.dp)
                        )
                    }
                }
            }
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(text = "Discover nearby routes")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isMarkingEnabled.value = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(text = "Add route")
                        }
                        Button(
                            onClick = {
                                if (markers.size != 0) {
                                    markers.removeLast()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No markers are placed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(text = "Delete last")
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val gpsLocation: Location? =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        val networkLocation: Location? =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val finalLocation = gpsLocation ?: networkLocation

        return if (finalLocation != null) {
            Pair(finalLocation.latitude, finalLocation.longitude)
        } else {
            null
        }
    }
}