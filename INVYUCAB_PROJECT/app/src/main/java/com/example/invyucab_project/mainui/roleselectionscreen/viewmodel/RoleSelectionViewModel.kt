package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 4.2 & 5.3.3: All verified user details are received
    val phone: String? = savedStateHandle.get<String>("phone")
    val email: String? = savedStateHandle.get<String>("email")
    val name: String? = savedStateHandle.get<String>("name")
    val gender: String? = savedStateHandle.get<String>("gender")
    val dob: String? = savedStateHandle.get<String>("dob")

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone, Email=$email, Name=$name, Gender=$gender, DOB=$dob")
    }

    // 5.1, 5.2, 5.3: Handle navigation based on role
    fun onRoleSelected(
        role: String,
        onNavigate: (route: String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d("RoleSelectionViewModel", "User selected role: $role. Saving...")

            // TODO: Implement API call to save the role

            // Determine navigation route
            when (role) {
                "Rider" -> {
                    // 5.1.1: Navigate to HomeScreen
                    onNavigate(Screen.HomeScreen.route)
                }
                "Admin" -> {
                    // 5.2.1: Navigate to AdminScreen
                    onNavigate(Screen.AdminScreen.route)
                }
                "Driver" -> {
                    // 5.3.1: Navigate to DriverDetailsScreen, passing all data
                    onNavigate(
                        Screen.DriverDetailsScreen.createRoute(
                            phone = phone,
                            email = email,
                            name = name,
                            gender = gender,
                            dob = dob
                        )
                    )
                }
            }
        }
    }
}