package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 5.3.3: Auto-filled data
    val name: String = savedStateHandle.get<String>("name") ?: ""
    val email: String = savedStateHandle.get<String>("email") ?: ""
    val phone: String = savedStateHandle.get<String>("phone") ?: ""
    val gender: String = savedStateHandle.get<String>("gender") ?: ""
    val dob: String = savedStateHandle.get<String>("dob") ?: ""

    // 5.3.4: New driver-specific fields
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
        // Basic validation, can be improved with regex
        if (vehicleNumber.isBlank()) {
            vehicleError = "Vehicle number is required"
            return false
        }
        vehicleError = null
        return true
    }

    private fun validateLicence(): Boolean {
        // Basic validation
        if (licenceNumber.isBlank()) {
            licenceError = "Licence number is required"
            return false
        }
        licenceError = null
        return true
    }

    // 5.3.5: Action
    fun onSubmitClicked(onNavigate: () -> Unit) {
        val isAadhaarValid = validateAadhaar()
        val isVehicleValid = validateVehicle()
        val isLicenceValid = validateLicence()

        if (isAadhaarValid && isVehicleValid && isLicenceValid) {
            // 5.3.7: Save complete account
            Log.d("DriverDetailsViewModel", "Saving Driver Details:")
            Log.d("DriverDetailsViewModel", "Personal: $name, $phone, $email, $gender, $dob")
            Log.d("DriverDetailsViewModel", "Driver: $aadhaarNumber, $vehicleNumber, $licenceNumber")

            // TODO: Implement API call to save all details

            // 5.3.6: Navigation
            onNavigate()
        }
    }
}