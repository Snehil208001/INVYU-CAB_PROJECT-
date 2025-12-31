package com.example.invyucab_project.data.models

import androidx.annotation.Keep
import com.squareup.moshi.Json

/**
 * Request body for creating a new ride.
 */
@Keep
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
 */
@Keep
data class CreateRideResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: Any? = null
)

@Keep
data class RideData(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "user_pin") val userPin: Int
)

// --- Total Rides Models ---

@Keep
data class DriverTotalRidesRequest(
    @Json(name = "driver_id") val driverId: Int
)

@Keep
data class DriverTotalRidesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: List<RideHistoryItem>? = null
)

@Keep
data class RideHistoryItem(
    @Json(name = "ride_id") val rideId: Int?,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "pickup_location") val pickupLocation: String? = null,
    @Json(name = "drop_address") val dropAddress: String? = null,
    @Json(name = "drop_location") val dropLocation: String? = null,
    @Json(name = "total_amount") val totalAmount: Any? = null,
    @Json(name = "price") val price: Any? = null,
    @Json(name = "amount") val amount: Any? = null,
    @Json(name = "estimated_price") val estimatedPrice: Any? = null,
    @Json(name = "fare") val fare: Any? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "pickup_latitude") val pickupLatitude: Any? = null,
    @Json(name = "pickup_longitude") val pickupLongitude: Any? = null,
    @Json(name = "drop_latitude") val dropLatitude: Any? = null,
    @Json(name = "drop_longitude") val dropLongitude: Any? = null
)

// --- Ongoing Rides Models (Driver Side) ---

@Keep
data class DriverOngoingRidesRequest(
    @Json(name = "driver_id") val driverId: Int
)

@Keep
data class DriverOngoingRidesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<OngoingRideItem>? = null
)

@Keep
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
    @Json(name = "drop_longitude") val dropLongitude: String? = null,
    @Json(name = "rider_mobile_number") val riderMobileNumber: String? = null
)

// --- Start Ride Models ---

@Keep
data class StartRideRequest(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "rider_id") val riderId: Int,
    @Json(name = "driver_id") val driverId: Int,
    @Json(name = "user_pin") val userPin: Int,
    @Json(name = "started_at") val startedAt: String
)

@Keep
data class StartRideResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: BookingDetailsData? = null
)

@Keep
data class BookingDetailsData(
    @Json(name = "otp") val otp: String? = null,
    @Json(name = "driver_name") val driverName: String? = null,
    @Json(name = "vehicle_model") val vehicleModel: String? = null,
    @Json(name = "vehicle_number") val vehicleNumber: String? = null
)

// --- Update Ride Status Models ---

@Keep
data class UpdateRideStatusRequest(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "status") val status: String
)

@Keep
data class UpdateRideStatusResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: Any? = null
)

// --- Rider Ongoing Ride Models ---

@Keep
data class RiderOngoingRideRequest(
    @Json(name = "ride_id") val rideId: Int
)

@Keep
data class RiderOngoingRideResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<RiderOngoingRideItem>? = null
)

@Keep
data class RiderOngoingRideItem(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "status") val status: String? = null,
    @Json(name = "driver_id") val driverId: Int? = null,
    @Json(name = "rider_id") val riderId: Int? = null,
    @Json(name = "driver_name") val driverName: String? = null,
    @Json(name = "vehicle_number") val vehicleNumber: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "user_pin") val userPin: Int? = null,
    @Json(name = "pickup_latitude") val pickupLatitude: String? = null,
    @Json(name = "pickup_longitude") val pickupLongitude: String? = null,
    @Json(name = "drop_latitude") val dropLatitude: String? = null,
    @Json(name = "drop_longitude") val dropLongitude: String? = null,
    @Json(name = "driver_photo") val driverPhoto: String? = null,
    @Json(name = "driver_rating") val driverRating: String? = null,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "drop_address") val dropAddress: String? = null,
    @Json(name = "estimated_price") val estimatedPrice: String? = null,
    @Json(name = "driver_phone") val driverPhone: String? = null,
    @Json(name = "rider_mobile_number") val riderMobileNumber: String? = null
)

// --- Local Data Models ---

@Keep
data class DriverUpcomingRideItem(
    val rideId: Int?,
    val riderId: Int? = null,
    val pickupAddress: String? = null,
    val dropAddress: String? = null,
    val pickupLatitude: String? = null,
    val pickupLongitude: String? = null,
    val dropLatitude: String? = null,
    val dropLongitude: String? = null,
    val estimatedPrice: String? = null,
    val status: String? = null,
    val distance: String? = null,
    val date: String? = null,
    val pickupLocation: String? = null,
    val dropLocation: String? = null,
    val fare: String? = null,
    val totalPrice: String? = null,
    val price: String? = null,
    val amount: String? = null,
    val totalAmount: String? = null,
    val estimatedFare: String? = null,
    val cost: String? = null,
    val createdAt: String? = null
)