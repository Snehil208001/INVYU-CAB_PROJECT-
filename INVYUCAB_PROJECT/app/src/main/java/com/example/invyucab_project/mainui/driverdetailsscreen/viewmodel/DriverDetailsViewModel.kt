package com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
// import androidx.lifecycle.viewModelScope // ❌ No longer needed
import com.example.invyucab_project.core.base.BaseViewModel
// import com.example.invyucab_project.core.common.Resource // ❌ No longer needed
import com.example.invyucab_project.core.navigations.Screen
// import com.example.invyucab_project.data.models.CreateUserRequest // ❌ No longer needed
// import com.example.invyucab_project.domain.usecase.CreateUserUseCase // ❌ No longer needed
// import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase // ❌ No longer needed
import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.flow.launchIn // ❌ No longer needed
// import kotlinx.coroutines.flow.onEach // ❌ No longer needed
// import kotlinx.coroutines.launch // ❌ No longer needed
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
// import java.text.SimpleDateFormat // ❌ No longer needed
// import java.util.Locale // ❌ No longer needed
import javax.inject.Inject

@HiltViewModel
class DriverDetailsViewModel @Inject constructor(
    // ❌ Usecases removed, no longer creating user here
    // private val createUserUseCase: CreateUserUseCase,
    // private val saveUserStatusUseCase: SaveUserStatusUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // ✅✅✅ START OF CHANGE ✅✅✅
    // All user details are received from UserDetailsScreen
    val phone: String? = savedStateHandle.get<String>("phone")
    val role: String? = savedStateHandle.get<String>("role")

    // Handle decoding
    val name: String? = try {
        val encoded: String? = savedStateHandle.get<String>("name")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("name")
    }
    val email: String? = try {
        val encoded: String? = savedStateHandle.get<String>("email")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("email")
    }
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
    // ✅✅✅ END OF CHANGE ✅✅✅


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
        Log.d("DriverDetailsVM", "Received: $phone, $role, $name, $email, $gender, $dob")
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

    // ❌ Removed formatDobForApi

    fun onSubmitClicked() {
        if (!validate()) {
            return
        }
        _apiError.value = null
        _isLoading.value = true

        // ✅✅✅ START OF CHANGE ✅✅✅
        // Logic removed. This ViewModel now just navigates to OtpScreen
        // It passes ALL data: user details + driver details

        Log.d("DriverDetailsVM", "Validation success. Navigating to OTP Screen.")

        sendEvent(UiEvent.Navigate(
            Screen.OtpScreen.createRoute(
                phone = phone!!,
                isSignUp = true,
                role = role!!,
                // ✅✅✅ THIS IS THE FIX ✅✅✅
                email = email?.ifBlank { null }, // Use safe call ?.
                // ✅✅✅ END OF FIX ✅✅✅
                name = name,
                gender = gender,
                dob = dob,
                license = licenceNumber,
                vehicle = vehicleNumber,
                aadhaar = aadhaarNumber
            )
        ))
        // ✅✅✅ END OF CHANGE ✅✅✅
    }
}