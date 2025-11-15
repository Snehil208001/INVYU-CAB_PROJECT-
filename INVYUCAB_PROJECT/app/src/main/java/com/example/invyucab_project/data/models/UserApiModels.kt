package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ... (All other existing models like CheckUserRequest, CreateUserRequest, etc.)

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
    @Json(name = "user_id") val userId: String,
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

// ✅✅✅ START OF FIX ✅✅✅
// Changed this data class to match the server response
@JsonClass(generateAdapter = true)
data class AddVehicleResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: Int? // This is the '16' from the log
)
// ✅✅✅ END OF FIX ✅✅✅