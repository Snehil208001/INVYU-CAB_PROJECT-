package com.example.invyucab_project.domain.model

import com.google.android.gms.maps.model.LatLng

data class AutocompletePrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val description: String // Full text
)

// Enum to track which text field is active
enum class SearchField {
    PICKUP, DROP
}

// ✅ ADDED: Model for Recent Rides
data class RecentRide(
    val rideId: Int,
    val pickupAddress: String,
    val dropAddress: String
)

data class HomeUiState(
    val currentLocation: LatLng? = null,
    val isFetchingLocation: Boolean = true,

    val pickupQuery: String = "Your Current Location",
    val dropQuery: String = "",
    val pickupPlaceId: String? = "current_location",
    val dropPlaceId: String? = null,

    val pickupResults: List<AutocompletePrediction> = emptyList(),
    val dropResults: List<AutocompletePrediction> = emptyList(),

    val recentDropLocations: List<AutocompletePrediction> = emptyList(),

    val isSearching: Boolean = false,
    val activeField: SearchField = SearchField.DROP,

    // ✅ ADDED: List to hold the latest 3 rides
    val recentRides: List<RecentRide> = emptyList()
)