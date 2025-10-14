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

    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""

    var otp by mutableStateOf("")
        private set

    fun onOtpChange(value: String) {
        // This ensures the state is updated only with valid input,
        // which is crucial for the UI to recompose and show your typing.
        if (value.length <= 4 && value.all { it.isDigit() }) {
            otp = value
        }
    }

    fun onVerifyClicked() {
        // TODO: Implement OTP verification logic
        if (otp.length == 4) {
            println("Verification successful for OTP: $otp on number: $fullPhoneNumber")
        } else {
            println("Invalid OTP")
        }
    }
}