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
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    val profileOptions = listOf(
        ProfileOption(Icons.Default.AccountCircle, "Edit Profile") { },
        ProfileOption(Icons.Default.CreditCard, "Payment Methods") { },
        ProfileOption(Icons.Default.History, "Ride History") { },
        ProfileOption(Icons.Default.Settings, "Settings") { },
        ProfileOption(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support") { },
        ProfileOption(Icons.AutoMirrored.Filled.Logout, "Logout") { }
    )

    fun logout() {
        viewModelScope.launch {
            // This invokes the logic to clear shared preferences
            logoutUserUseCase.invoke()
            Log.d("ProfileViewModel", "Logout successful")
        }
    }
}