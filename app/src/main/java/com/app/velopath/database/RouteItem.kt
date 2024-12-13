package com.app.velopath.database

data class RouteItem(
    val id: String,
    val title: String,
    val author: String,
    val navigationLink: String,
    val startLang: Double,
    val startLong: Double,
    val endLang: Double,
    val endLong: Double,
    val distance: Double,
    val duration: Int,
    val overviewPolyline: String,
    val isExpanded: Boolean = false
)

data class Stats(
    val totalAdd: Int = 0,
    val currentAdd: Int = 0,
    val totalLiked: Int = 0,
    val totalRoutes: Int = 0
)