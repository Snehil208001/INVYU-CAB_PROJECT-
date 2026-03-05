package com.example.invyucab_project.data.models

import com.google.android.gms.maps.model.LatLng

data class RideBookingUiState(
    val isLoading: Boolean = true,
    val isCancelling: Boolean = false, // ✅ State for cancel button loader
    val errorMessage: String? = null,
    val rideId: String? = null,
    val userPin: String? = null,

    val pickupLocation: LatLng? = null,
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Loading...",
    val dropDescription: String = "Loading...",
    val routePolyline: List<LatLng> = emptyList(),

    val isSearchingForDriver: Boolean = true,

    // ✅ ADDED: Track search progress (0 = Initial, 1 = Busy, 2 = Not Found)
    val searchProgressState: Int = 0
)