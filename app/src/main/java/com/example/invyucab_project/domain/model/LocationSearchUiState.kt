// This file contains UI state models for the LocationSearchScreen.
// These models are used by the LocationSearchViewModel.
package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

// Represents a location result in the search list
data class SearchLocation(
    val name: String,
    val address: String,
    val icon: ImageVector,
    val placeId: String // To pass to the next screen
)

// Tracks which text field (pickup or drop) is currently active
enum class EditingField {
    PICKUP,
    DROP
}