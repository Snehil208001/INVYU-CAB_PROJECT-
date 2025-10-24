package com.example.invyucab_project.data.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Data classes for Google Place Details API response (simplified)

@JsonClass(generateAdapter = true)
data class PlaceDetailsResponse(
    @Json(name = "result") val result: PlaceResult?,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class PlaceResult(
    @Json(name = "geometry") val geometry: Geometry?
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "location") val location: PlaceLocation?
)

@JsonClass(generateAdapter = true)
data class PlaceLocation(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)