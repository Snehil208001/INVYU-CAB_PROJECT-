package com.example.invyucab_project.data.models

// You can replace 'VehicleData' with a more specific data class
// if you need to use the vehicle details elsewhere.
// For now, we just need to check if 'data' is null or not.
data class VehicleData(
    val vehicle_id: String?,
    val vehicle_name: String?,
    val vehicle_number: String?,
    val vehicle_type: String?
)

data class VehicleDetailsResponse(
    val status: String?,
    val message: String?,
    val data: VehicleData? // Assuming the API returns a 'data' object if a vehicle exists
)