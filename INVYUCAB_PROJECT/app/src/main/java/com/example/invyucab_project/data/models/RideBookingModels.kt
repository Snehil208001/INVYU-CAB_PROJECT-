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
 * ✅ CHANGED: 'data' is now Any? to handle both Integer (broken backend) and Object (fixed backend)
 */
data class CreateRideResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: Any? = null
)

data class RideData(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "user_pin") val userPin: Int
)

// --- Total Rides Models ---

data class DriverTotalRidesRequest(
    @Json(name = "driver_id") val driverId: Int
)

data class DriverTotalRidesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: List<RideHistoryItem>? = null
)

data class RideHistoryItem(
    @Json(name = "ride_id") val rideId: Int?,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "pickup_location") val pickupLocation: String? = null,
    @Json(name = "drop_address") val dropAddress: String? = null,
    @Json(name = "drop_location") val dropLocation: String? = null,
    @Json(name = "total_amount") val totalAmount: Any? = null,
    @Json(name = "price") val price: Any? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// --- Ongoing Rides Models ---

data class DriverOngoingRidesRequest(
    @Json(name = "driver_id") val driverId: Int
)

data class DriverOngoingRidesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<OngoingRideItem>? = null
)

data class OngoingRideItem(
    @Json(name = "ride_id") val rideId: Int?,
    @Json(name = "rider_id") val riderId: Int? = null,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "pickup_location") val pickupLocation: String? = null,
    @Json(name = "drop_address") val dropAddress: String? = null,
    @Json(name = "drop_location") val dropLocation: String? = null,
    @Json(name = "price") val price: Any? = null,
    @Json(name = "total_amount") val totalAmount: Any? = null,
    @Json(name = "estimated_price") val estimatedPrice: Any? = null,
    @Json(name = "pickup_latitude") val pickupLatitude: String? = null,
    @Json(name = "pickup_longitude") val pickupLongitude: String? = null,
    @Json(name = "drop_latitude") val dropLatitude: String? = null,
    @Json(name = "drop_longitude") val dropLongitude: String? = null
)

// --- Start Ride Models ---

data class StartRideRequest(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "rider_id") val riderId: Int,
    @Json(name = "driver_id") val driverId: Int,
    @Json(name = "user_pin") val userPin: Int,
    @Json(name = "started_at") val startedAt: String
)

data class StartRideResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: BookingDetailsData? = null
)

data class BookingDetailsData(
    @Json(name = "otp") val otp: String? = null,
    @Json(name = "driver_name") val driverName: String? = null,
    @Json(name = "vehicle_model") val vehicleModel: String? = null,
    @Json(name = "vehicle_number") val vehicleNumber: String? = null
)