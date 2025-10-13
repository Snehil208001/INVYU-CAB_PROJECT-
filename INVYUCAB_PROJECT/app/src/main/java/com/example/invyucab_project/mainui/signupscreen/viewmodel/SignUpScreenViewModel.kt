package com.example.invyucab_project.mainui.signupscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor() : ViewModel() {
    // --- Common Fields for Both User and Driver ---
    var fullName by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // --- Driver Specific Fields ---
    var vehicleModel by mutableStateOf("")
        private set
    var vehicleNumber by mutableStateOf("")
        private set
    var licenseNumber by mutableStateOf("")
        private set

    // --- Event Handlers ---
    fun onFullNameChange(value: String) { fullName = value }
    fun onPhoneChange(value: String) { phone = value }
    fun onEmailChange(value: String) { email = value }
    fun onPasswordChange(value: String) { password = value }
    fun onVehicleModelChange(value: String) { vehicleModel = value }
    fun onVehicleNumberChange(value: String) { vehicleNumber = value }
    fun onLicenseNumberChange(value: String) { licenseNumber = value }

    fun onUserSignUpClicked() {
        // TODO: Implement user sign-up logic
    }

    fun onDriverSignUpClicked() {
        // TODO: Implement driver sign-up logic
    }
}