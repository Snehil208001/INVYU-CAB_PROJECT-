package com.example.invyucab_project.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

// Represents a clickable option item on the profile screen
data class ProfileOption(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit = {}
)

// ✅ Updated: Removed hardcoded "Snehil" and fixed default values
data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val profilePicUrl: String? = null
)