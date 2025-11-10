// This file contains UI state models for the RideSelectionScreen.
// These models are used by the RideSelectionViewModel.
package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng

// ✅ MODIFIED RideOption: price is now added
// Represents a single ride choice (e.g., Bike, Auto, Cab)
data class RideOption(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val description: String, // ETA like "2 mins away"
    val price: String? = null, // ✅ RE-ADDED
    val subtitle: String? = null,
    val estimatedDurationMinutes: Int? = null, // Trip duration
    val estimatedDistanceKm: String? = null,
    val isLoadingPrice: Boolean = true // ✅ ADDED
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
    val isLoading: Boolean = false,
    val isFetchingLocation: Boolean = true,
    val errorMessage: String? = null
)