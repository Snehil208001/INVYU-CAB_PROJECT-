package com.example.invyucab_project.mainui.driverprofilescreen.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
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
class DriverProfileViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase,
    private val checkUserUseCase: CheckUserUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : BaseViewModel() {

    // ✅ Dynamic State for Driver Name (starts empty or with a placeholder)
    private val _driverName = MutableStateFlow("Loading...")
    val driverName: StateFlow<String> = _driverName

    // ✅ Dynamic State for Phone Number
    private val _driverPhoneNumber = MutableStateFlow("")
    val driverPhoneNumber: StateFlow<String> = _driverPhoneNumber

    // We can also fetch the profile image dynamically later, keeping placeholder for now
    val profileImageUrl: String = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=100&h=100&fit=crop&crop=faces"

    init {
        fetchDriverProfile()
    }

    private fun fetchDriverProfile() {
        val storedPhone = userPreferencesRepository.getPhoneNumber()

        if (!storedPhone.isNullOrEmpty()) {
            // CheckUserUseCase adds +91, so we pass the number.
            // The UseCase update handles stripping if necessary, but good to be clean here.
            checkUserUseCase(storedPhone).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val status = result.data
                        if (status is UserCheckStatus.Exists) {
                            // ✅ Update the name and phone from the API response
                            _driverName.value = status.name
                            _driverPhoneNumber.value = status.phoneNumber
                        }
                    }
                    is Resource.Error -> {
                        _driverName.value = "Driver" // Fallback on error
                        _driverPhoneNumber.value = storedPhone // Show stored phone on error
                    }
                    is Resource.Loading -> {
                        // Optional: Show loading state if needed
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            _driverName.value = "Guest Driver"
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUserUseCase.invoke()
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}