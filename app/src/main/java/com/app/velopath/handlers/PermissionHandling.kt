package com.app.velopath.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

fun getCurrentLocation(context: Context): Pair<Double, Double>? {
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