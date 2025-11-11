package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // All user details are received from UserDetailsScreen
    val phone: String? = savedStateHandle.get<String>("phone")
    val role: String? = savedStateHandle.get<String>("role")

    val name: String? = try {
        val encoded: String? = savedStateHandle.get<String>("name")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("name")
    }

    // ❌ REMOVED email
    /*
    val email: String? = try {
        val encoded: String? = savedStateHandle.get<String>("email")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("email")
    }
    */

    val gender: String? = try {
        val encoded: String? = savedStateHandle.get<String>("gender")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("gender")
    }
    val dob: String? = try {
        val encoded: String? = savedStateHandle.get<String>("dob")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("dob")
    }

    // --- Form State ---
    var aadhaarNumber by mutableStateOf("")
        private set
    var licenceNumber by mutableStateOf("")
        private set
    var vehicleNumber by mutableStateOf("")
        private set

    // --- Error State ---
    var aadhaarError by mutableStateOf<String?>(null)
        private set
    var licenceError by mutableStateOf<String?>(null)
        private set
    var vehicleError by mutableStateOf<String?>(null)
        private set

    init {
        // ❌ REMOVED email from log
        Log.d("DriverDetailsVM", "Received: $phone, $role, $name, $gender, $dob")
    }

    // --- Event Handlers ---
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

    fun onVehicleChange(value: String) {
        vehicleNumber = value.uppercase()
        vehicleError = if (value.isBlank()) "Vehicle number is required" else null
    }

    private fun validate(): Boolean {
        aadhaarError = if (aadhaarNumber.length != 12) "Aadhaar must be 12 digits" else null
        licenceError = if (licenceNumber.isBlank()) "Licence is required" else null
        vehicleError = if (vehicleNumber.isBlank()) "Vehicle number is required" else null

        return aadhaarError == null && licenceError == null && vehicleError == null
    }

    fun onSubmitClicked() {
        if (!validate()) {
            return
        }
        _apiError.value = null
        _isLoading.value = true

        Log.d("DriverDetailsVM", "Validation success. Navigating to OTP Screen.")

        sendEvent(UiEvent.Navigate(
            Screen.OtpScreen.createRoute(
                phone = phone!!,
                isSignUp = true,
                role = role!!,
                // email = email?.ifBlank { null }, // ❌ REMOVED
                name = name,
                gender = gender,
                dob = dob,
                license = licenceNumber,
                vehicle = vehicleNumber,
                aadhaar = aadhaarNumber
            )
        ))
    }
}