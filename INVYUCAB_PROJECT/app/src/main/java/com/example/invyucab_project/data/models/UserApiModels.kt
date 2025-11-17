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
    @Json(name = "user_id") val userId: Int, // Changed to Int to match new log
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


// --- Vehicle Details Models ---

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

// ✅✅✅ START OF FIX ✅✅✅
// Making this data class robust to handle the inconsistent backend responses
@JsonClass(generateAdapter = true)
data class GetVehicleDetailsResponse(
    @Json(name = "success") val success: Boolean?, // Made nullable
    @Json(name = "succcess") val succcess: Boolean?, // Added typo field
    @Json(name = "isDriverPresent") val isDriverPresent: Boolean?, // Added new field
    @Json(name = "data") val data: List<VehicleDetails>?, // Keep as List
    @Json(name = "error") val error: String? // Added error field
)
// ✅✅✅ END OF FIX ✅✅✅