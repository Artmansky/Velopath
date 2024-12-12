package com.app.velopath.handlers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun getCurrentLocation(
    context: Context,
    onFail: (() -> Unit)? = null
): Pair<Double, Double>? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (!locationManager.isLocationEnabled) {
        onFail?.invoke()
        return null
    }

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    val finalLocation: Location? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
        }

        else -> {
            val gpsLocation: Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation: Location? =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            gpsLocation ?: networkLocation
        }
    }

    return if (finalLocation != null) {
        Pair(finalLocation.latitude, finalLocation.longitude)
    } else {
        null
    }
}

fun openLocationSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}