package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Check User ---

@JsonClass(generateAdapter = true)
data class CheckUserRequest(
    @Json(name = "phone_number") val phoneNumber: String
)

// This data class matches the 'existing_user' object in the API response
@JsonClass(generateAdapter = true)
data class ExistingUser(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "user_role") val userRole: String?,
    @Json(name = "gender") val gender: String?,
    @Json(name = "dob") val dob: String?,
    @Json(name = "status") val status: String?
    // Add other fields from the log if needed, but keep them nullable
)

// Modified CheckUserResponse to use 'existingUser' (nullable)
// instead of 'userExists' (boolean)
@JsonClass(generateAdapter = true)
data class CheckUserResponse(
    @Json(name = "message") val message: String,
    @Json(name = "existing_user") val existingUser: ExistingUser?
)


// --- Create User ---

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone_number") val phoneNumber: String, // e.g., "+919876543210"
    @Json(name = "user_role") val userRole: String, // "rider", "driver", or "admin"
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "gender") val gender: String?,
    @Json(name = "dob") val dob: String?, // e.g., "1999-07-21"
    @Json(name = "license_number") val licenseNumber: String? = null,
    @Json(name = "vehicle_id") val vehicleId: String? = null,
    @Json(name = "rating") val rating: Double? = 4.5, // Default rating
    @Json(name = "wallet_balance") val walletBalance: Double? = 0.0,
    @Json(name = "is_verified") val isVerified: Boolean? = false,
    @Json(name = "status") val status: String? = "pending"
)

@JsonClass(generateAdapter = true)
data class CreateUserResponse(
    @Json(name = "user_id") val userId: String,
    @Json(name = "message") val message: String
)

// --- Update User Status ---

@JsonClass(generateAdapter = true)
data class UpdateUserStatusRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "status") val status: String, // e.g., "active"
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateUserStatusResponse(
    @Json(name = "message") val message: String
)

// --- Get Pricing ---

@JsonClass(generateAdapter = true)
data class GetPricingRequest(
    @Json(name = "pickup_lat") val pickupLat: Double,
    @Json(name = "pickup_lng") val pickupLng: Double,
    @Json(name = "drop_lat") val dropLat: Double,
    @Json(name = "drop_lng") val dropLng: Double
)

// ✅✅✅ START OF FIX (Problem 2) ✅✅✅
// Updated this class to match the log response
@JsonClass(generateAdapter = true)
data class RidePrice(
    @Json(name = "vehicle_name") val vehicle_name: String?, // Changed from rideType
    @Json(name = "total_price") val total_price: Double // Changed from price
)

// Updated this class to match the log response
@JsonClass(generateAdapter = true)
data class GetPricingResponse(
    @Json(name = "success") val success: Boolean, // Changed from status
    @Json(name = "data") val data: List<RidePrice>? // Changed from prices
)
// ✅✅✅ END OF FIX (Problem 2) ✅✅✅