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

    // ✅ MODIFIED: Retrieve all 5 user details
    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""
    val email: String? = savedStateHandle.get<String>("email")
    val name: String? = savedStateHandle.get<String>("name")       // ✅ ADDED
    val gender: String? = savedStateHandle.get<String>("gender")   // ✅ ADDED
    val dob: String? = savedStateHandle.get<String>("dob")         // ✅ ADDED

    private val isSignUp: Boolean = savedStateHandle.get<Boolean>("isSignUp") ?: false

    var otp by mutableStateOf("")
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun onOtpChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            otp = value
            error = null
        }
    }

    // ✅ MODIFIED: Callback now includes all user details
    fun onVerifyClicked(
        onNavigateToRoleSelection: (phone: String, email: String?, name: String?, gender: String?, dob: String?) -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        if (otp.length != 4) {
            error = "OTP must be 4 digits"
            return
        }

        // TODO: Implement real OTP verification logic
        if (true) { // Simulating success
            println("Verification successful for OTP: $otp on number: $fullPhoneNumber")
            error = null

            if (isSignUp) {
                // NEW USER -> Navigate to RoleSelectionScreen with all data
                onNavigateToRoleSelection(fullPhoneNumber, email, name, gender, dob)
            } else {
                // EXISTING USER -> Navigate directly to Home
                onNavigateToHome()
            }
        } else {
            error = "The OTP entered is incorrect"
        }
    }
}