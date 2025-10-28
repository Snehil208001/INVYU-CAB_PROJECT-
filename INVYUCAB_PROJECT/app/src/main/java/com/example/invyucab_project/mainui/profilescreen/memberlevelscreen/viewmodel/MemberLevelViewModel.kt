package com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MemberLevel(
    val level: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color
)

@HiltViewModel
class MemberLevelViewModel @Inject constructor() : ViewModel() {

    // This would be fetched from the user's data
    private val _currentUserLevel = MutableStateFlow("Gold Member")
    val currentUserLevel: StateFlow<String> = _currentUserLevel

    private val _levels = MutableStateFlow(listOf(
        MemberLevel(
            level = "Bronze Member",
            description = "Complete 5 rides to unlock Silver",
            icon = Icons.Default.MilitaryTech,
            iconColor = Color(0xFFCD7F32)
        ),
        MemberLevel(
            level = "Silver Member",
            description = "Complete 20 rides to unlock Gold",
            icon = Icons.Default.Star,
            iconColor = Color(0xFFC0C0C0)
        ),
        MemberLevel(
            level = "Gold Member",
            description = "Get exclusive discounts & priority support",
            icon = Icons.Default.Diamond,
            iconColor = Color(0xFFFFD700)
        )
    ))
    val levels: StateFlow<List<MemberLevel>> = _levels

}