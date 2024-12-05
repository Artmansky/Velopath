package com.app.velopath.handlers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


fun getDirections(
    origin: LatLng,
    destination: LatLng,
    waypoints: List<LatLng>?,
    apiKey: String,
    callback: (List<LatLng>?) -> Unit
) {
    val originString = "${origin.latitude},${origin.longitude}"
    val destinationString = "${destination.latitude},${destination.longitude}"
    val waypointsString =
        waypoints?.joinToString(separator = "|") { "${it.latitude},${it.longitude}" }

    val call = RetrofitClient.apiService.getDirections(
        origin = originString,
        destination = destinationString,
        waypoints = waypointsString,
        apiKey = apiKey
    )

    call.enqueue(object : Callback<DirectionsJson> {
        override fun onResponse(call: Call<DirectionsJson>, response: Response<DirectionsJson>) {
            if (response.isSuccessful) {
                val directionsResponse = response.body()
                if (directionsResponse != null && directionsResponse.status == "OK") {
                    val polylinePoints =
                        PolyUtil.decode(directionsResponse.routes[0].overview_polyline.points)
                    Log.d("DirectionsAPI", "Decoded polyline points: ")
                    polylinePoints.forEach {
                        Log.d("DirectionsAPI", "Lat: ${it.latitude}, Lng: ${it.longitude}")
                    }
                    callback(polylinePoints)  // Call the callback with the decoded points
                } else {
                    Log.d("DirectionsAPI", "No routes found or status not OK")
                    callback(null)  // Return null if no routes are found
                }
            } else {
                Log.d("DirectionsAPI", "Error: ${response.code()}")
                callback(null)  // Return null on error
            }
        }

        override fun onFailure(call: Call<DirectionsJson>, t: Throwable) {
            Log.e("DirectionsAPI", "Failed to get directions: ${t.message}")
            callback(null)  // Return null on failure
        }
    })
}