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

    var otp by mutableStateOf("")
        private set

    fun onOtpChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            otp = value
        }
    }

    // ✅ MODIFIED: Added an onSuccess callback to handle navigation
    fun onVerifyClicked(onSuccess: () -> Unit) {
        // TODO: Implement real OTP verification logic
        if (otp.length == 4) {
            println("Verification successful for OTP: $otp on number: $fullPhoneNumber")
            // Call the callback on success
            onSuccess()
        } else {
            println("Invalid OTP")
        }
    }
}