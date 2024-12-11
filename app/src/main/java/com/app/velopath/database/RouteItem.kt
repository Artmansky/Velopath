package com.app.velopath.database

data class RouteItem(
    val title: String,
    val author: Int,
    val id: String,
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