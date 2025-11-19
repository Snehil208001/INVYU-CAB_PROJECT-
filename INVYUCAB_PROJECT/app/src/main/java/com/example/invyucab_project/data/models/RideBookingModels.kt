package com.example.invyucab_project.data.models

import com.squareup.moshi.Json

/**
 * Request body for creating a new ride.
 */
data class CreateRideRequest(
    @Json(name = "rider_id") val riderId: Int,
    @Json(name = "driver_id") val driverId: Int? = null,
    @Json(name = "pickup_latitude") val pickupLatitude: Double,
    @Json(name = "pickup_longitude") val pickupLongitude: Double,
    @Json(name = "drop_latitude") val dropLatitude: Double,
    @Json(name = "drop_longitude") val dropLongitude: Double,
    @Json(name = "estimated_price") val estimatedPrice: Double,
    @Json(name = "actual_price") val actualPrice: Double? = null,
    @Json(name = "status") val status: String = "requested",
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

/**
 * Response body for the create ride API.
 * Updated to match the server response: {"success":true,"data":6}
 */
data class CreateRideResponse(
    @Json(name = "success") val success: Boolean,
    // The server returns the ID (Int) inside the "data" field
    @Json(name = "data") val rideId: Int
)