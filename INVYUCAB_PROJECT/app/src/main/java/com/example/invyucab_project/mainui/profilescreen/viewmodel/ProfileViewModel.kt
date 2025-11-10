package com.example.invyucab_project.mainui.profilescreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector // Keep this import for Icons
import androidx.lifecycle.ViewModel
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
// Refactored: UI state classes imported from domain.model
import com.example.invyucab_project.domain.model.ProfileOption
import com.example.invyucab_project.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// Note: ProfileOption and UserProfile were moved to
// domain/model/ProfileUiState.kt


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // ✅ INJECTED FirebaseAuth
    private val userPreferencesRepository: UserPreferencesRepository // ✅ INJECTED
) : ViewModel() {

    // StateFlow to hold user profile information (can be loaded from repository later)
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    // Define the list of profile options
    val profileOptions = listOf(
        ProfileOption(Icons.Default.AccountCircle, "Edit Profile") { /* Handled in UI */ },
        ProfileOption(Icons.Default.CreditCard, "Payment Methods") { /* Handled in UI */ },
        ProfileOption(Icons.Default.History, "Ride History") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.Default.Settings, "Settings") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.AutoMirrored.Filled.Logout, "Logout") { }
    )

    // ✅ MODIFIED: Implement actual logout logic
    fun logout() {
        firebaseAuth.signOut()
        println("Logout clicked and user signed out")

        // ✅✅✅ NEW: Clear status from SharedPreferences ✅✅✅
        userPreferencesRepository.clearUserStatus()
        println("User status cleared from SharedPreferences")
        // ✅✅✅ END OF NEW CODE ✅✅✅
    }

    // You might add functions here later to load user data from a repository
    // init {
    //     loadUserProfile()
    // }
    //
    // private fun loadUserProfile() {
    //     viewModelScope.launch {
    //         // Replace with actual data fetching
    //         _userProfile.value = UserProfile(name = "Fetched Name", phone = "+91 1234567890")
    //     }
    // }
}