// This file contains UI state models for the RideSelectionScreen.
// These models are used by the RideSelectionViewModel.
package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng

// ✅ This data class is correct as you provided it.
data class RideOption(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val description: String, // ETA like "2 mins away"
    val price: String? = null,
    val subtitle: String? = null,
    val estimatedDurationMinutes: Int? = null, // Trip duration
    val estimatedDistanceKm: String? = null,
    val isLoadingPrice: Boolean = true
)

// Represents the overall state of the RideSelectionScreen
data class RideSelectionState(
    val pickupLocation: LatLng? = null,
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Fetching current location...",
    val dropDescription: String = "",
    val routePolyline: List<LatLng> = emptyList(),
    val tripDurationSeconds: Int? = null,
    val tripDistanceMeters: Int? = null,

    // ✅✅✅ FIX FOR CONFLICTS ✅✅✅
    // The ride options list should be PART of the main UI state.
    val rideOptions: List<RideOption> = emptyList(),

    val isLoading: Boolean = false,
    val isFetchingLocation: Boolean = true,
    val errorMessage: String? = null
)