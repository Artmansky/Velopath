package com.app.velopath.handlers

import com.google.android.gms.maps.model.LatLng


fun buildDirectionsUrl(
    origin: LatLng,
    destination: LatLng,
    apiKey: String,
    waypoints: List<LatLng>? = null
): String {
    val baseUrl = "https://maps.googleapis.com/maps/api/directions/json"
    val originParam = "origin=${origin.latitude},${origin.longitude}"
    val destinationParam = "destination=${destination.latitude},${destination.longitude}"
    val waypointsParam =
        waypoints?.joinToString(separator = "|") { "${it.latitude},${it.longitude}" }
            ?.let { "waypoints=$it" }

    val queryParams = listOfNotNull(
        originParam,
        destinationParam,
        "mode=bicycling",
        waypointsParam,
        "key=$apiKey"
    ).joinToString("&")

    return "$baseUrl?$queryParams"
}