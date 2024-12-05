package com.app.velopath.handlers

data class DirectionsJson(
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