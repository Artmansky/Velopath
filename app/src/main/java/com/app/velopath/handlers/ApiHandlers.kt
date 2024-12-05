package com.app.velopath.handlers

import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.app.velopath.handlers.directions.DirectionsJson
import com.app.velopath.handlers.directions.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiHandlers(private val context: Context) {
    private val apiKey = getString(context, R.string.google_directions_api)

    var distance: Double = 0.0
    var duration: Double = 0.0

    fun getDirections(
        markers: List<LatLng>,
        callback: (List<LatLng>?) -> Unit
    ) {
        if (!isNetworkAvailable(context)) callback(null)

        val originString = "${markers.first().latitude},${markers.first().longitude}"
        val destinationString = "${markers.last().latitude},${markers.last().longitude}"
        val waypointsString = markers
            .drop(1)
            .dropLast(1)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|") { "${it.latitude},${it.longitude}" }

        val call = RetrofitClient.apiService.getDirections(
            origin = originString,
            destination = destinationString,
            waypoints = waypointsString,
            apiKey = apiKey
        )

        call.enqueue(object : Callback<DirectionsJson> {
            override fun onResponse(
                call: Call<DirectionsJson>,
                response: Response<DirectionsJson>
            ) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    if (directionsResponse != null && directionsResponse.status == "OK") {
                        val polylinePoints =
                            PolyUtil.decode(directionsResponse.routes[0].overview_polyline.points)

                        //Dodadac nogi do siebie

                        callback(polylinePoints)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<DirectionsJson>, t: Throwable) {
                callback(null)
            }
        })
    }
}