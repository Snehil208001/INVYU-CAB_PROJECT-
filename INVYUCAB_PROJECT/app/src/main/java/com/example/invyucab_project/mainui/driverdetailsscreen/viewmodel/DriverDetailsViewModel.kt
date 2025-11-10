package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel // ✅ IMPORTED
import com.example.invyucab_project.core.common.Resource // ✅ IMPORTED
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.domain.usecase.CreateUserUseCase // ✅ IMPORTED
import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase // ✅ IMPORTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase, // ✅ INJECTED USECASE
    private val saveUserStatusUseCase: SaveUserStatusUseCase, // ✅ INJECTED USECASE
    savedStateHandle: SavedStateHandle
) : BaseViewModel() { // ✅ INHERITS FROM BASEVIEWMODEL

    // Auto-filled data (unchanged)
    val name: String = savedStateHandle.get<String>("name") ?: ""
    val email: String = savedStateHandle.get<String>("email") ?: ""
    val phone: String = savedStateHandle.get<String>("phone") ?: ""
    val gender: String = savedStateHandle.get<String>("gender") ?: ""
    val rawDob: String = savedStateHandle.get<String>("dob") ?: ""

    // Driver-specific fields (unchanged)
    var aadhaarNumber by mutableStateOf("")
        private set
    var vehicleNumber by mutableStateOf("")
        private set
    var licenceNumber by mutableStateOf("")
        private set

    // Error states for new fields (unchanged)
    var aadhaarError by mutableStateOf<String?>(null)
        private set
    var vehicleError by mutableStateOf<String?>(null)
        private set
    var licenceError by mutableStateOf<String?>(null)
        private set

    // ⛔ 'isLoading' and 'apiError' are now inherited from BaseViewModel

    // Helper to convert date format (unchanged)
    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e("DriverDetailsViewModel", "Could not parse date: $dobString", e)
            null
        }
    }

    // --- All on-change and validation functions are unchanged ---
    fun onAadhaarChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 12) {
            aadhaarNumber = value
            if (aadhaarError != null) validateAadhaar()
        }
    }
    fun onVehicleChange(value: String) {
        vehicleNumber = value.uppercase()
        if (vehicleError != null) validateVehicle()
    }
    fun onLicenceChange(value: String) {
        licenceNumber = value.uppercase()
        if (licenceError != null) validateLicence()
    }
    private fun validateAadhaar(): Boolean {
        if (aadhaarNumber.length != 12) {
            aadhaarError = "Aadhaar must be 12 digits"
            return false
        }
        aadhaarError = null
        return true
    }
    private fun validateVehicle(): Boolean {
        if (vehicleNumber.isBlank()) {
            vehicleError = "Vehicle number is required"
            return false
        }
        vehicleError = null
        return true
    }
    private fun validateLicence(): Boolean {
        if (licenceNumber.isBlank()) {
            licenceError = "Licence number is required"
            return false
        }
        licenceError = null
        return true
    }

    // ✅ REFACTORED: Calls UseCases and emits navigation events
    fun onSubmitClicked() {
        val isAadhaarValid = validateAadhaar()
        val isVehicleValid = validateVehicle()
        val isLicenceValid = validateLicence()

        if (isAadhaarValid && isVehicleValid && isLicenceValid) {
            Log.d("DriverDetailsViewModel", "Saving Driver Details...")
            val formattedDob = formatDobForApi(rawDob)

            val request = CreateUserRequest(
                fullName = name,
                phoneNumber = "+91$phone",
                userRole = "driver",
                profilePhotoUrl = null,
                gender = gender.lowercase(),
                dob = formattedDob,
                licenseNumber = licenceNumber,
                vehicleId = vehicleNumber,
                rating = null,         // Explicitly null
                walletBalance = null,  // Explicitly null
                isVerified = true,
                status = "active"
            )

            createUserUseCase.invoke(request).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        Log.d("DriverDetailsViewModel", "Driver user created successfully.")

                        // Save status and navigate
                        viewModelScope.launch {
                            saveUserStatusUseCase.invoke("active")
                            Log.d("DriverDetailsViewModel", "Driver status 'active' saved to SharedPreferences.")
                            sendEvent(UiEvent.Navigate(Screen.DriverScreen.route))
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _apiError.value = result.message
                        Log.e("DriverDetailsViewModel", "Failed to create driver: ${result.message}")
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}