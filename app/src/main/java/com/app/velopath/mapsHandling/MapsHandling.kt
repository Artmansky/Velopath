package com.app.velopath.mapsHandling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions

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
        val mapViewportState = rememberMapViewportState()

        val userLocation = remember {
            if (hasPermission) getCurrentLocation(context) else null
        } ?: defaultLocation

        //Poprawic wyswietlanie poczatkowej pozycji na mapie
        mapViewportState.setCameraOptions {
            center(Point.fromLngLat(userLocation.second, userLocation.first))
            zoom(15.0)
            pitch(0.0)
        }

        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.75f)
            ) {
                MapboxMap(
                    modifier = modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = {
                        if (darkMode) {
                            MapStyle(style = Style.DARK)
                        } else {
                            MapStyle(style = Style.LIGHT)
                        }
                    }
                ) {
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)

                            enabled = hasPermission

                            puckBearing = PuckBearing.COURSE

                            puckBearingEnabled = hasPermission
                        }
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
                            //Dodac sprawdzanie czy puck dostepny
                            mapViewportState.transitionToFollowPuckState(
                                FollowPuckViewportStateOptions.Builder()
                                    .pitch(0.0)
                                    .zoom(15.0)
                                    .build()
                            )
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

    private fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        return if (location != null) {
            Pair(location.latitude, location.longitude)
        } else {
            null
        }
    }
}