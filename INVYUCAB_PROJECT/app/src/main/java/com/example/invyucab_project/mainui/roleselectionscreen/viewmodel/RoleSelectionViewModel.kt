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

        Log.d("RoleSelectionViewModel", "User selected role: $role. Navigating to UserDetails...")

        sendEvent(UiEvent.Navigate(
            Screen.UserDetailsScreen.createRoute(
                phone = phone!!,
                role = role,
                // email = null, // ❌ REMOVED
                name = null
            )
        ))

        _isLoading.value = false
    }
}