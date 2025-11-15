package com.example.invyucab_project.mainui.driverscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase
) : BaseViewModel() {

    var isActive by mutableStateOf(false)
        private set

    fun onActiveToggleChanged(active: Boolean) {
        this.isActive = active
        viewModelScope.launch {
            if (active) {
                Log.d("DriverViewModel", "Driver is now ACTIVE")
                // TODO: Call API to set driver status to "active"
            } else {
                Log.d("DriverViewModel", "Driver is now INACTIVE")
                // TODO: Call API to set driver status to "inactive"
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // Clear the locally saved user status
            logoutUserUseCase()
            // ✅✅✅ FIX: Removed the popUpTo lambda. Just send the route.
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}