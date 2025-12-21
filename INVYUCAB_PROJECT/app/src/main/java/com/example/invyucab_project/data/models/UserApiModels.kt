package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckUserRequest(
    @Json(name = "phone_number") val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class ExistingUser(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "user_role") val userRole: String?,
    @Json(name = "gender") val gender: String?,
    @Json(name = "dob") val dob: String?,
    @Json(name = "status") val status: String?
)

@JsonClass(generateAdapter = true)
data class CheckUserResponse(
    @Json(name = "message") val message: String,
    @Json(name = "existing_user") val existingUser: ExistingUser?
)

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "user_role") val userRole: String,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "gender") val gender: String?,
    @Json(name = "dob") val dob: String?,
    @Json(name = "license_number") val licenseNumber: String? = null,
    @Json(name = "vehicle_id") val vehicleId: String? = null,
    @Json(name = "rating") val rating: Double? = 4.5,
    @Json(name = "wallet_balance") val walletBalance: Double? = 0.0,
    @Json(name = "is_verified") val isVerified: Boolean? = false,
    @Json(name = "status") val status: String? = "pending"
)

@JsonClass(generateAdapter = true)
data class CreateUserResponse(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserStatusRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "status") val status: String,
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateUserStatusResponse(
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class GetPricingRequest(
    @Json(name = "pickup_lat") val pickupLat: Double,
    @Json(name = "pickup_lng") val pickupLng: Double,
    @Json(name = "drop_lat") val dropLat: Double,
    @Json(name = "drop_lng") val dropLng: Double
)

@JsonClass(generateAdapter = true)
data class RidePrice(
    @Json(name = "vehicle_name") val vehicle_name: String?,
    @Json(name = "total_price") val total_price: Double
)

@JsonClass(generateAdapter = true)
data class GetPricingResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<RidePrice>?
)

@JsonClass(generateAdapter = true)
data class AddVehicleRequest(
    @Json(name = "driver_id") val driverId: String,
    @Json(name = "vehicle_number") val vehicleNumber: String,
    @Json(name = "model") val model: String,
    @Json(name = "type") val type: String,
    @Json(name = "color") val color: String,
    @Json(name = "capacity") val capacity: String
)

@JsonClass(generateAdapter = true)
data class AddVehicleResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: Int?
)

@JsonClass(generateAdapter = true)
data class GetVehicleDetailsRequest(
    @Json(name = "driver_id") val driverId: String
)

@JsonClass(generateAdapter = true)
data class VehicleDetails(
    @Json(name = "vehicle_id") val vehicleId: Int?,
    @Json(name = "vehicle_number") val vehicleNumber: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "color") val color: String?,
    @Json(name = "capacity") val capacity: String?
)

@JsonClass(generateAdapter = true)
data class GetVehicleDetailsResponse(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "succcess") val succcess: Boolean?,
    @Json(name = "isDriverPresent") val isDriverPresent: Boolean?,
    @Json(name = "data") val data: List<VehicleDetails>?,
    @Json(name = "error") val error: String?
)

@JsonClass(generateAdapter = true)
data class UpdateDriverLocationRequest(
    @Json(name = "driver_id") val driverId: Int,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "is_active") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class UpdateDriverLocationResponse(
    @Json(name = "message") val message: String?,
    @Json(name = "success") val success: Boolean?
)

@JsonClass(generateAdapter = true)
data class DriverUpcomingRidesRequest(
    @Json(name = "driver_id") val driverId: Int,
    @Json(name = "driver_latitude") val driverLatitude: String,
    @Json(name = "driver_longitude") val driverLongitude: String
)

@JsonClass(generateAdapter = true)
data class UpcomingRide(
    @Json(name = "ride_id") val rideId: Int?,

    @Json(name = "pickup_latitude") val pickupLatitude: String?,
    @Json(name = "pickup_longitude") val pickupLongitude: String?,
    @Json(name = "drop_latitude") val dropLatitude: String?,
    @Json(name = "drop_longitude") val dropLongitude: String?,

    @Json(name = "pickup_address") val pickupAddress: String?,
    @Json(name = "pickup_location") val pickupLocation: String?,
    @Json(name = "drop_address") val dropAddress: String?,
    @Json(name = "drop_location") val dropLocation: String?,

    @Json(name = "status") val status: String? = null,
    @Json(name = "requested_at") val requestedAt: String? = null,

    @Json(name = "estimated_price") val estimatedPrice: Any?,
    @Json(name = "fare") val fare: Any?,
    @Json(name = "total_price") val totalPrice: Any?,
    @Json(name = "price") val price: Any?,
    @Json(name = "amount") val amount: Any?,
    @Json(name = "total_amount") val totalAmount: Any?,
    @Json(name = "estimated_fare") val estimatedFare: Any?,
    @Json(name = "cost") val cost: Any?
)

@JsonClass(generateAdapter = true)
data class DriverUpcomingRidesResponse(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "data") val data: List<UpcomingRide>?,
    @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class AcceptRideRequest(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "driver_id") val driverId: Int
)

@JsonClass(generateAdapter = true)
data class AcceptRideResponse(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class RideHistoryRequest(
    @Json(name = "user_id") val userId: Int
)

@JsonClass(generateAdapter = true)
data class RiderRideHistoryItem(
    @Json(name = "ride_id") val rideId: Int,
    @Json(name = "rider_id") val riderId: Int?,
    @Json(name = "driver_id") val driverId: Int?,
    @Json(name = "pickup_latitude") val pickupLatitude: String?,
    @Json(name = "pickup_longitude") val pickupLongitude: String?,
    @Json(name = "drop_latitude") val dropLatitude: String?,
    @Json(name = "drop_longitude") val dropLongitude: String?,
    @Json(name = "estimated_price") val estimatedPrice: String?,
    @Json(name = "actual_price") val actualPrice: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "requested_at") val requestedAt: String?,
    @Json(name = "started_at") val startedAt: String?,
    @Json(name = "completed_at") val completedAt: String?,
    @Json(name = "driver_name") val driverName: String?,
    @Json(name = "driver_photo") val driverPhoto: String?,
    @Json(name = "driver_rating") val driverRating: String?,
    @Json(name = "vehicle_number") val vehicleNumber: String?,
    @Json(name = "model") val model: String?
)

@JsonClass(generateAdapter = true)
data class RiderRideHistoryResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: List<RiderRideHistoryItem>?
)

@JsonClass(generateAdapter = true)
data class UpdateFcmTokenRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "fcm_token") val fcmToken: String
)

@JsonClass(generateAdapter = true)
data class UpdateFcmTokenResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String
)

// ✅ ADDED: Models for Twilio Masked Calling
@JsonClass(generateAdapter = true)
data class InitiateCallRequest(
    @Json(name = "from_number") val fromNumber: String,
    @Json(name = "to_number") val toNumber: String
)

@JsonClass(generateAdapter = true)
data class InitiateCallResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String
)