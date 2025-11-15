package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // Received from RoleSelectionScreen
    val phone: String? = savedStateHandle.get<String>("phone")
    val role: String? = savedStateHandle.get<String>("role")

    // --- Personal Details Form State ---
    var name by mutableStateOf("")
        private set
    var gender by mutableStateOf("")
        private set
    var dob by mutableStateOf("") // This will be birthday
        private set

    // --- Driver Details Form State ---
    var aadhaarNumber by mutableStateOf("")
        private set
    var licenceNumber by mutableStateOf("")
        private set

    // --- Vehicle Details Form State (Matches API Body, minus driver_id) ---
    var vehicleNumber by mutableStateOf("")
        private set
    var vehicleModel by mutableStateOf("")
        private set
    var vehicleType by mutableStateOf("") // "Auto", "Bike", "Car"
        private set
    var vehicleColor by mutableStateOf("")
        private set
    var vehicleCapacity by mutableStateOf("")
        private set

    // --- Error State ---
    var nameError by mutableStateOf<String?>(null)
        private set
    var genderError by mutableStateOf<String?>(null)
        private set
    var dobError by mutableStateOf<String?>(null)
        private set
    var aadhaarError by mutableStateOf<String?>(null)
        private set
    var licenceError by mutableStateOf<String?>(null)
        private set
    var vehicleNumberError by mutableStateOf<String?>(null)
        private set
    var vehicleModelError by mutableStateOf<String?>(null)
        private set
    var vehicleTypeError by mutableStateOf<String?>(null)
        private set
    var vehicleColorError by mutableStateOf<String?>(null)
        private set
    var vehicleCapacityError by mutableStateOf<String?>(null)
        private set

    val vehicleTypes = listOf("Auto", "Bike", "Car")

    init {
        Log.d("DriverDetailsVM", "Received: $phone, $role")
    }

    // --- Event Handlers ---
    fun onNameChange(value: String) {
        name = value
        nameError = if (value.isBlank()) "Name is required" else null
    }

    fun onGenderChange(value: String) {
        gender = value
        genderError = if (value.isBlank()) "Gender is required" else null
    }

    fun onDobChange(value: String) {
        dob = value
        dobError = if (value.isBlank()) "Date of birth is required" else null
    }

    fun onAadhaarChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 12) {
            aadhaarNumber = value
            aadhaarError = if (value.length != 12) "Must be 12 digits" else null
        }
    }

    fun onLicenceChange(value: String) {
        licenceNumber = value.uppercase()
        licenceError = if (value.isBlank()) "Licence is required" else null
    }

    fun onVehicleNumberChange(value: String) {
        vehicleNumber = value.uppercase()
        vehicleNumberError = if (value.isBlank()) "Vehicle number is required" else null
    }

    fun onVehicleModelChange(value: String) {
        vehicleModel = value
        vehicleModelError = if (value.isBlank()) "Model is required" else null
    }

    fun onVehicleTypeChange(value: String) {
        vehicleType = value
        vehicleTypeError = if (value.isBlank()) "Type is required" else null
    }

    fun onVehicleColorChange(value: String) {
        vehicleColor = value
        vehicleColorError = if (value.isBlank()) "Color is required" else null
    }

    fun onVehicleCapacityChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 2) {
            vehicleCapacity = value
            val capacityInt = value.toIntOrNull()
            vehicleCapacityError = when {
                value.isBlank() -> "Capacity is required"
                capacityInt == null || capacityInt <= 0 -> "Must be > 0"
                else -> null
            }
        }
    }

    private fun validate(): Boolean {
        // Trigger validation for ALL fields
        onNameChange(name)
        onGenderChange(gender)
        onDobChange(dob)
        onAadhaarChange(aadhaarNumber)
        onLicenceChange(licenceNumber)
        onVehicleNumberChange(vehicleNumber)
        onVehicleModelChange(vehicleModel)
        onVehicleTypeChange(vehicleType)
        onVehicleColorChange(vehicleColor)
        onVehicleCapacityChange(vehicleCapacity)

        // Check all errors
        return nameError == null && genderError == null && dobError == null &&
                aadhaarError == null && licenceError == null &&
                vehicleNumberError == null && vehicleModelError == null &&
                vehicleTypeError == null && vehicleColorError == null &&
                vehicleCapacityError == null
    }

    fun onSubmitClicked() {
        if (!validate()) {
            return
        }
        _apiError.value = null
        _isLoading.value = true

        Log.d("DriverDetailsVM", "Validation success. Navigating to OTP Screen.")

        // Pass ALL collected data to the OtpScreen route
        sendEvent(UiEvent.Navigate(
            Screen.OtpScreen.createRoute(
                phone = phone!!,
                isSignUp = true,
                role = role!!,
                name = name,
                gender = gender,
                dob = dob,
                license = licenceNumber,
                aadhaar = aadhaarNumber,
                // Vehicle fields
                vehicleNumber = vehicleNumber,
                vehicleModel = vehicleModel,
                vehicleType = vehicleType,
                vehicleColor = vehicleColor,
                vehicleCapacity = vehicleCapacity
            )
        ))
    }
}