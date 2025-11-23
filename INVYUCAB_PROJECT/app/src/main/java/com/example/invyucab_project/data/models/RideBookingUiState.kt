package com.example.invyucab_project.data.models

import com.google.android.gms.maps.model.LatLng

data class RideBookingUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val rideId: String? = null,
    val userPin: String? = null, // ✅ ADDED: To store the PIN

    val pickupLocation: LatLng? = null,
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Loading...",
    val dropDescription: String = "Loading...",
    val routePolyline: List<LatLng> = emptyList(),

    val isSearchingForDriver: Boolean = true
    // We will add driver details here later, e.g.:
    // val driverName: String? = null,
    // val driverVehicle: String? = null,
    // val driverRating: Float? = null,
)