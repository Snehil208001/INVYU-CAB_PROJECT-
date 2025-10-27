package com.example.invyucab_project.mainui.otpscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ✅ MODIFIED: Retrieve both phone and the new optional email
    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""
    val email: String? = savedStateHandle.get<String>("email")

    // ✅ ADDED: Read the 'isSignUp' flag from the navigation route
    private val isSignUp: Boolean = savedStateHandle.get<Boolean>("isSignUp") ?: false

    var otp by mutableStateOf("")
        private set

    var error by mutableStateOf<String?>(null) // Error state
        private set

    fun onOtpChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            otp = value
            error = null // Clear error on change
        }
    }

    // ✅ MODIFIED: Added an onSuccess callback to handle navigation
    fun onVerifyClicked(
        onNavigateToHome: () -> Unit,
        onNavigateToDetails: () -> Unit
    ) {
        if (otp.length != 4) {
            error = "OTP must be 4 digits"
            return
        }

        // TODO: Implement real OTP verification logic
        // if (api.verify(otp) == true) {
        if (true) { // Simulating success
            println("Verification successful for OTP: $otp on number: $fullPhoneNumber")
            error = null

            // --- THIS IS THE NEW LOGIC ---
            if (isSignUp) {
                // User is SIGNING UP -> Go to UserDetails
                onNavigateToDetails()
            } else {
                // User is SIGNING IN -> Go to Home
                onNavigateToHome()
            }
            // -----------------------------

        } else {
            // else { // Simulating failure
            //    error = "The OTP entered is incorrect"
            // }
        }
    }
}