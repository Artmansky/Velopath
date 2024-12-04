package com.app.velopath.handlers

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


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

fun test(context: Context) {

    val apiKey = getString(context, R.string.google_directions_api)


    val origin = "33.8121,-117.919"
    val destination = "34.1381,-118.3534"
    val waypoints = "34.0242,-118.4965|33.985,-118.4695" // opcjonalne

    getDirections(origin, destination, waypoints, apiKey)

}

interface DirectionsApiService {

    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,
        @Query("mode") mode: String = "bicycling",  // domyślnie ustawiamy "bicycling"
        @Query("avoid") avoid: String = "highways",  // domyślnie ustawiamy "highways"
        @Query("key") apiKey: String
    ): Call<DirectionsDto>
}

data class DirectionsDto(
    val routes: List<Route>,
    val status: String
) {
    data class Route(
        val overview_polyline: OverviewPolyline,
        val legs: List<Leg>,
    ) {


        data class Leg(
            val distance: Distance,
            val duration: Duration,

            ) {
            data class Distance(
                val text: String,
                val value: Int
            )

            data class Duration(
                val text: String,
                val value: Int
            )

        }

        data class OverviewPolyline(
            val points: String
        )
    }
}

fun getDirections(origin: String, destination: String, waypoints: String?, apiKey: String) {
    val call = RetrofitClient.apiService.getDirections(
        origin = origin,
        destination = destination,
        waypoints = waypoints,
        apiKey = apiKey
    )

    call.enqueue(object : Callback<DirectionsDto> {
        override fun onResponse(call: Call<DirectionsDto>, response: Response<DirectionsDto>) {
            if (response.isSuccessful) {
                val directionsResponse = response.body()
                if (directionsResponse != null && directionsResponse.status == "OK") {
                    // Wyświetl dane trasy
                    Log.d("DirectionsAPI", "Found ${directionsResponse.routes.size} routes.")
                    for (route in directionsResponse.routes) {
                        for (leg in route.legs) {
                            // Wypisujemy distance i duration
                            Log.d(
                                "DirectionsAPI",
                                "Distance: ${leg.distance.text}, Duration: ${leg.duration.text}"
                            )
                        }
                        // Dekodowanie polyline i wyświetlanie punktów
                        val polylinePoints = PolyUtil.decode(route.overview_polyline.points)
                        Log.d("DirectionsAPI", "Decoded polyline points: ")
                        for (latLng in polylinePoints) {
                            Log.d(
                                "DirectionsAPI",
                                "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
                            )
                        }

                    }
                } else {
                    Log.d("DirectionsAPI", "No routes found or status not OK")
                }
            } else {
                Log.d("DirectionsAPI", "Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<DirectionsDto>, t: Throwable) {
            Log.e("DirectionsAPI", "Failed to get directions: ${t.message}")
        }
    })
}

object RetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: DirectionsApiService = retrofit.create(DirectionsApiService::class.java)
}