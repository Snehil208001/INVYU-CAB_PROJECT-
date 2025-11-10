package com.example.invyucab_project.mainui.profilescreen.viewmodel

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.domain.model.ProfileOption
import com.example.invyucab_project.domain.model.UserProfile
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase // ✅ INJECTED USECASE
) : ViewModel() { // ⬅️ Does not inherit from BaseViewModel

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    // Define the list of profile options
    val profileOptions = listOf(
        ProfileOption(Icons.Default.AccountCircle, "Edit Profile") { /* Handled in UI */ },
        ProfileOption(Icons.Default.CreditCard, "Payment Methods") { /* Handled in UI */ },
        ProfileOption(Icons.Default.History, "Ride History") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.Default.Settings, "Settings") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support") { /* TODO: Navigate/Action */ },
        ProfileOption(Icons.AutoMirrored.Filled.Logout, "Logout") { } // Click is handled by UI
    )

    // ✅ REFACTORED: Calls UseCase
    fun logout() {
        viewModelScope.launch {
            logoutUserUseCase.invoke()
            Log.d("ProfileViewModel", "Logout successful")
            // Navigation should be handled in the UI by observing a state
            // or by the click handler in the UI itself
        }
    }
}