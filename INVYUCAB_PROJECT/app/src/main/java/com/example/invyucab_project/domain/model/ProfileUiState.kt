// This file contains UI state models for the ProfileScreen.
// These models are used by the ProfileViewModel.
package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

// Represents a clickable option item on the profile screen
data class ProfileOption(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit = {} // Placeholder for navigation/action
)

// Represents the user's information displayed at the top of the profile screen
data class UserProfile(
    val name: String = "Snehil", // Placeholder
    val phone: String = "+91 7542957884", // Placeholder
    val profilePicUrl: String? = null // Placeholder, could be URL or local resource ID
)