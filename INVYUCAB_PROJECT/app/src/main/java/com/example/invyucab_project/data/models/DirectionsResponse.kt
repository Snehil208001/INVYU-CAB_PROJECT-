package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Data classes for Google Directions API response

@JsonClass(generateAdapter = true)
data class DirectionsResponse(
    @Json(name = "routes") val routes: List<Route>,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Route(
    @Json(name = "overview_polyline") val overviewPolyline: OverviewPolyline
)

@JsonClass(generateAdapter = true)
data class OverviewPolyline(
    @Json(name = "points") val points: String
)