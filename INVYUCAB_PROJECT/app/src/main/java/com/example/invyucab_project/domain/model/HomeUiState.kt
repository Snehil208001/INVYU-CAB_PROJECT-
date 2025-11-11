package com.example.invyucab_project.domain.model

import com.google.android.gms.maps.model.LatLng

// ✅ ADDED This data class back. It was deleted when LocationSearchScreen was removed.
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

data class HomeUiState(
    val currentLocation: LatLng? = null,
    val isFetchingLocation: Boolean = true, // We still need this for the API

    // ✅ ADDED properties to handle the new search UI
    val pickupQuery: String = "Your Current Location",
    val dropQuery: String = "",
    val pickupPlaceId: String? = "current_location", // Default to current location
    val dropPlaceId: String? = null,

    val pickupResults: List<AutocompletePrediction> = emptyList(),
    val dropResults: List<AutocompletePrediction> = emptyList(),

    // ✅✅✅ START OF FIX ✅✅✅
    // This property was missing, causing the error in HomeScreen.kt
    val recentDropLocations: List<AutocompletePrediction> = emptyList(),
    // ✅✅✅ END OF FIX ✅✅✅

    val isSearching: Boolean = false,
    val activeField: SearchField = SearchField.DROP // Drop is active by default
)