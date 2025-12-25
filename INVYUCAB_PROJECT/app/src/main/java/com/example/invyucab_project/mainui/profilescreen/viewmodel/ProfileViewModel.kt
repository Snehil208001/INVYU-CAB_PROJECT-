package com.example.invyucab_project.mainui.profilescreen.viewmodel

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.model.ProfileOption
import com.example.invyucab_project.domain.model.UserProfile
import com.example.invyucab_project.domain.usecase.CheckUserUseCase
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import com.example.invyucab_project.domain.usecase.UserCheckStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase,
    private val checkUserUseCase: CheckUserUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : BaseViewModel() { // ✅ Inherit from BaseViewModel for consistency

    // ✅ Start with a loading placeholder or empty state
    private val _userProfile = MutableStateFlow(UserProfile(name = "Loading...", phone = ""))
    val userProfile: StateFlow<UserProfile> = _userProfile

    val profileOptions = listOf(
        ProfileOption(Icons.Default.AccountCircle, "Edit Profile") { },
        ProfileOption(Icons.Default.CreditCard, "Payment Methods") { },
        ProfileOption(Icons.Default.History, "Ride History") { },
        ProfileOption(Icons.Default.Settings, "Settings") { },
        ProfileOption(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support") { },
        ProfileOption(Icons.AutoMirrored.Filled.Logout, "Logout") { }
    )

    init {
        fetchUserProfile()
    }

    // ✅ New Function: Fetch user details using the stored phone number
    private fun fetchUserProfile() {
        val storedPhone = userPreferencesRepository.getPhoneNumber()

        if (!storedPhone.isNullOrEmpty()) {
            checkUserUseCase(storedPhone).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val status = result.data
                        if (status is UserCheckStatus.Exists) {
                            // ✅ Update the state with the real name from the API
                            _userProfile.value = _userProfile.value.copy(
                                name = status.name,
                                phone = storedPhone
                            )
                        }
                    }
                    is Resource.Error -> {
                        _userProfile.value = _userProfile.value.copy(name = "User")
                    }
                    else -> {}
                }
            }.launchIn(viewModelScope)
        } else {
            _userProfile.value = _userProfile.value.copy(name = "Guest", phone = "")
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUserUseCase.invoke()
            Log.d("ProfileViewModel", "Logout successful")
        }
    }
}