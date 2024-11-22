package com.app.velopath.mapsHandling

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

class MapsHandling(private val context: Context) {
    private var userLocation = Point.fromLngLat(-73.977001, 40.728847)

    @Composable
    fun PrintMap(darkMode: Boolean) {
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

        ShowMap(darkMode, hasPermission)
    }

    @Composable
    private fun ShowMap(darkMode: Boolean, hasPermission: Boolean) {

        val currentLocation: Pair<Double, Double> = when {
            hasPermission -> getCurrentLocation(context) ?: Pair(
                userLocation.longitude(),
                userLocation.latitude()
            )

            else -> Pair(userLocation.longitude(), userLocation.latitude())
        }
        
        if (hasPermission) userLocation =
            Point.fromLngLat(currentLocation.first, currentLocation.second)

        val mapViewportState = rememberMapViewportState {
            setCameraOptions {
                center(Point.fromLngLat(currentLocation.first, currentLocation.second))
                zoom(15.0)
                pitch(0.0)
            }
        }

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
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

                    puckBearingEnabled = true
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

        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        return if (location != null) {
            Pair(location.longitude, location.latitude)
        } else {
            return null
        }
    }
}