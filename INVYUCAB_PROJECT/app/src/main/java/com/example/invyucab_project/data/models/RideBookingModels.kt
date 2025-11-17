package com.example.invyucab_project.data.models

import com.squareup.moshi.Json // ✅ CORRECTED: Using Moshi import

/**
 * Request body for creating a new ride
 * This matches the JSON body you provided.
 */
data class CreateRideRequest(
    @field:Json(name = "rider_id") val riderId: Int, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "driver_id") val driverId: Int? = null, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "pickup_latitude") val pickupLatitude: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "pickup_longitude") val pickupLongitude: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "drop_latitude") val dropLatitude: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "drop_longitude") val dropLongitude: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "estimated_price") val estimatedPrice: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "actual_price") val actualPrice: Double? = null, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "status") val status: String = "requested", // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "started_at") val startedAt: String? = null, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "completed_at") val completedAt: String? = null // ✅ CORRECTED: Using @field:Json
)

/**
 * Response body after a ride is successfully created
 * NOTE: This is an assumption based on common API design.
 * You may need to update this if your API returns something different.
 */
data class CreateRideResponse(
    @field:Json(name = "ride_id") val rideId: Int, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "rider_id") val riderId: Int, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "driver_id") val driverId: Int?, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "status") val status: String, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "estimated_price") val estimatedPrice: Double, // ✅ CORRECTED: Using @field:Json
    @field:Json(name = "created_at") val createdAt: String // ✅ CORRECTED: Using @field:Json
)