package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DirectionsResponse(
    @Json(name = "routes") val routes: List<Route>,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Route(
    @Json(name = "overview_polyline") val overviewPolyline: OverviewPolyline,
    @Json(name = "legs") val legs: List<Leg> // ✅ ADD Legs for duration/distance
)

@JsonClass(generateAdapter = true)
data class OverviewPolyline(
    @Json(name = "points") val points: String
)

// ✅ ADD Leg, Duration, Distance classes
@JsonClass(generateAdapter = true)
data class Leg(
    @Json(name = "duration") val duration: DurationInfo,
    @Json(name = "distance") val distance: DistanceInfo // ✅ ADDED
)

@JsonClass(generateAdapter = true)
data class DurationInfo(
    @Json(name = "value") val value: Int // Duration in seconds
)

// ✅ ADDED
@JsonClass(generateAdapter = true)
data class DistanceInfo(
    @Json(name = "value") val value: Int // Distance in meters
)