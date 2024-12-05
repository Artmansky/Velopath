package com.app.velopath.handlers.directions

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {

    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,
        @Query("mode") mode: String = "bicycling",
        @Query("avoid") avoid: String = "highways",
        @Query("key") apiKey: String
    ): Call<DirectionsJson>

}