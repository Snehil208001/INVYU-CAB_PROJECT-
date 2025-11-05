package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.models.CreateUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val customApiService: CustomApiService, // ✅ INJECTED
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // All verified user details are received
    val phone: String? = savedStateHandle.get<String>("phone")
    val email: String? = savedStateHandle.get<String>("email")
    val name: String? = savedStateHandle.get<String>("name")
    val gender: String? = savedStateHandle.get<String>("gender")
    val dob: String? = savedStateHandle.get<String>("dob")

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone, Email=$email, Name=$name, Gender=$gender, DOB=$dob")
    }

    // ✅ MODIFIED: Calls createUser API
    fun onRoleSelected(
        role: String,
        onNavigate: (route: String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d("RoleSelectionViewModel", "User selected role: $role. Saving...")

            // For Driver, we navigate first to get more details
            if (role == "Driver") {
                onNavigate(
                    Screen.DriverDetailsScreen.createRoute(
                        phone = phone,
                        email = email,
                        name = name,
                        gender = gender,
                        dob = dob
                    )
                )
                return@launch
            }

            // For Rider or Admin, create the user now
            try {
                val request = CreateUserRequest(
                    fullName = name ?: "User",
                    phoneNumber = "+91$phone",
                    userRole = role.lowercase(), // "rider" or "admin"
                    profilePhotoUrl = null,
                    gender = gender?.lowercase(),
                    dob = dob, // Assuming DOB is already in "YYYY-MM-DD" format. If not, it needs parsing.
                    licenseNumber = null,
                    vehicleId = null,
                    isVerified = true, // They just verified with OTP
                    status = "active"  // They just verified with OTP
                )

                customApiService.createUser(request)
                Log.d("RoleSelectionViewModel", "$role user created successfully.")

                // Determine navigation route
                when (role) {
                    "Rider" -> onNavigate(Screen.HomeScreen.route)
                    "Admin" -> onNavigate(Screen.AdminScreen.route)
                }

            } catch (e: Exception) {
                Log.e("RoleSelectionViewModel", "Failed to create user: ${e.message}", e)
                // TODO: Show an error to the user on the UI
            }
        }
    }
}