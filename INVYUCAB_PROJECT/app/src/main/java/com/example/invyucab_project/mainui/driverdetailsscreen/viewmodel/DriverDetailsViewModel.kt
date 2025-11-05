package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.models.CreateUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    private val customApiService: CustomApiService, // ✅ INJECTED
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Auto-filled data
    val name: String = savedStateHandle.get<String>("name") ?: ""
    val email: String = savedStateHandle.get<String>("email") ?: ""
    val phone: String = savedStateHandle.get<String>("phone") ?: ""
    val gender: String = savedStateHandle.get<String>("gender") ?: ""
    val rawDob: String = savedStateHandle.get<String>("dob") ?: "" // ✅ Use rawDob

    // New driver-specific fields
    var aadhaarNumber by mutableStateOf("")
        private set
    var vehicleNumber by mutableStateOf("")
        private set
    var licenceNumber by mutableStateOf("")
        private set

    // Error states for new fields
    var aadhaarError by mutableStateOf<String?>(null)
        private set
    var vehicleError by mutableStateOf<String?>(null)
        private set
    var licenceError by mutableStateOf<String?>(null)
        private set

    // ✅ ADDED
    var isLoading by mutableStateOf(false)
        private set
    var apiError by mutableStateOf<String?>(null)
        private set

    // ✅ ADDED: Helper to convert date format
    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e("DriverDetailsViewModel", "Could not parse date: $dobString", e)
            null // Return null if parsing fails
        }
    }

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

    // ✅ MODIFIED: Calls createUser API
    fun onSubmitClicked(onNavigate: () -> Unit) {
        val isAadhaarValid = validateAadhaar()
        val isVehicleValid = validateVehicle()
        val isLicenceValid = validateLicence()

        if (isAadhaarValid && isVehicleValid && isLicenceValid) {
            viewModelScope.launch {
                isLoading = true
                apiError = null
                try {
                    // Save complete account
                    Log.d("DriverDetailsViewModel", "Saving Driver Details...")

                    val formattedDob = formatDobForApi(rawDob) // ✅ Format the date

                    val request = CreateUserRequest(
                        fullName = name,
                        phoneNumber = "+91$phone",
                        userRole = "driver",
                        profilePhotoUrl = null,
                        gender = gender.lowercase(),
                        dob = formattedDob, // ✅ Use formatted date
                        licenseNumber = licenceNumber,
                        vehicleId = vehicleNumber, // Using vehicleNumber for vehicle_id
                        isVerified = true,
                        status = "active"
                    )

                    customApiService.createUser(request)
                    Log.d("DriverDetailsViewModel", "Driver user created successfully.")

                    // Navigation
                    onNavigate()

                } catch (e: Exception) {
                    Log.e("DriverDetailsViewModel", "Failed to create driver: ${e.message}", e)
                    apiError = "Registration failed: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}