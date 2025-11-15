package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val phone: String? = savedStateHandle.get<String>("phone")

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone")
    }

    fun onRoleSelected(role: String) {
        _apiError.value = null
        _isLoading.value = true

        // ✅✅✅ START OF MODIFICATION ✅✅✅
        if (role.equals("Driver", ignoreCase = true)) {
            Log.d("RoleSelectionViewModel", "User selected role: $role. Navigating to DriverDetails...")
            // If DRIVER, skip UserDetailsScreen and go to DriverDetailsScreen
            sendEvent(UiEvent.Navigate(
                Screen.DriverDetailsScreen.createRoute(
                    phone = phone!!,
                    role = role
                )
            ))
        } else {
            Log.d("RoleSelectionViewModel", "User selected role: $role. Navigating to UserDetails...")
            // If RIDER (or other), go to UserDetailsScreen as before
            sendEvent(UiEvent.Navigate(
                Screen.UserDetailsScreen.createRoute(
                    phone = phone!!,
                    role = role,
                    name = null
                )
            ))
        }
        // ✅✅✅ END OF MODIFICATION ✅✅✅

        _isLoading.value = false
    }
}