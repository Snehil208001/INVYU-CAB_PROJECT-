package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.models.CreateUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
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
    private val rawDob: String? = savedStateHandle.get<String>("dob")

    // ✅ ADDED: State for loading and errors
    var isLoading by mutableStateOf(false)
        private set
    var apiError by mutableStateOf<String?>(null)
        private set

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone, Email=$email, Name=$name, Gender=$gender, DOB=$rawDob")
    }

    // ✅ ADDED: Helper to convert date format
    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e("RoleSelectionViewModel", "Could not parse date: $dobString", e)
            null // Return null if parsing fails
        }
    }

    // ✅ MODIFIED: Calls createUser API only for Rider/Admin
    fun onRoleSelected(
        role: String,
        onNavigate: (route: String) -> Unit
    ) {
        // Clear any previous error
        apiError = null

        // For Driver, we navigate first to get more details
        if (role == "Driver") {
            onNavigate(
                Screen.DriverDetailsScreen.createRoute(
                    phone = phone,
                    email = email,
                    name = name,
                    gender = gender,
                    dob = rawDob // Pass the raw DOB string
                )
            )
            return
        }

        // For Rider or Admin, create the user now
        viewModelScope.launch {
            isLoading = true
            Log.d("RoleSelectionViewModel", "User selected role: $role. Saving...")
            try {
                val formattedDob = formatDobForApi(rawDob)
                val request = CreateUserRequest(
                    fullName = name ?: "User",
                    phoneNumber = "+91$phone",
                    userRole = role.lowercase(), // "rider" or "admin"
                    profilePhotoUrl = null,
                    gender = gender?.lowercase(),
                    dob = formattedDob,
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
                apiError = "Registration failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}