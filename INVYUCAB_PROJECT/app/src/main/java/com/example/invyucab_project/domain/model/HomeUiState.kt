// This file contains UI state models for the HomeScreen.
// These models are used by the HomeViewModel.
package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

// Represents an item in the "Explore" section of the home screen
data class ExploreItem(val icon: ImageVector, val label: String)

// Represents a quick-access "Go Places" item on the home screen
data class PlaceItem(val icon: ImageVector, val label: String)

// Represents a recent location in the "Recent Locations" list
data class RecentLocation(val name: String, val address: String)