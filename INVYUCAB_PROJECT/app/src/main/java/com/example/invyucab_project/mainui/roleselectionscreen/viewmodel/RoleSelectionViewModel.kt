package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.domain.usecase.CreateUserUseCase
import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val saveUserStatusUseCase: SaveUserStatusUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // All verified user details are received
    val phone: String? = savedStateHandle.get<String>("phone")
    val email: String? = savedStateHandle.get<String>("email")
    val name: String? = savedStateHandle.get<String>("name")
    val gender: String? = savedStateHandle.get<String>("gender")

    private val rawDob: String? = try {
        val encodedDob: String? = savedStateHandle.get<String>("dob")
        encodedDob?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        Log.e("RoleSelectionViewModel", "Failed to decode DOB", e)
        savedStateHandle.get<String>("dob")
    }

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone, Email=$email, Name=$name, Gender=$gender, DOB=$rawDob")
    }

    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e("RoleSelectionViewModel", "Could not parse date: $dobString", e)
            null
        }
    }

    fun onRoleSelected(role: String) {
        _apiError.value = null

        // For Driver, we navigate first to get more details
        if (role == "Driver") {
            sendEvent(UiEvent.Navigate(
                Screen.DriverDetailsScreen.createRoute(
                    phone = phone,
                    email = email,
                    name = name,
                    gender = gender,
                    dob = rawDob
                )
            ))
            return
        }

        // For Rider or Admin, create the user now
        Log.d("RoleSelectionViewModel", "User selected role: $role. Saving...")

        val formattedDob = formatDobForApi(rawDob)

        // ✅✅✅ THE FIX IS HERE ✅✅✅
        // We must explicitly set all skipped optional parameters to null
        // to avoid the 'Inapplicable candidate' compiler error.
        val request = CreateUserRequest(
            fullName = name ?: "User",
            phoneNumber = "+91$phone",
            userRole = role.lowercase(), // "rider" or "admin"
            profilePhotoUrl = null,
            gender = gender?.lowercase(),
            dob = formattedDob,
            licenseNumber = null,
            vehicleId = null,
            rating = null,         // ✅ ADDED THIS LINE
            walletBalance = null,  // ✅ ADDED THIS LINE
            isVerified = true,
            status = "active"
        )
        // ✅✅✅ END OF FIX ✅✅✅

        createUserUseCase.invoke(request).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    Log.d("RoleSelectionViewModel", "$role user created successfully.")

                    // Save status and navigate
                    viewModelScope.launch {
                        saveUserStatusUseCase.invoke("active")
                        Log.d("RoleSelectionViewModel", "User status 'active' saved to SharedPreferences.")

                        val route = when (role) {
                            "Rider" -> Screen.HomeScreen.route
                            "Admin" -> Screen.AdminScreen.route
                            else -> Screen.HomeScreen.route
                        }
                        sendEvent(UiEvent.Navigate(route))
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _apiError.value = result.message
                }
            }
        }.launchIn(viewModelScope)
    }
}