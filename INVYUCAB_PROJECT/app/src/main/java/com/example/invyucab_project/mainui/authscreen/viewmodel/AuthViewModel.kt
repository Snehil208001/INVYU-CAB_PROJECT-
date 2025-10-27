package com.example.invyucab_project.mainui.authscreen.viewmodel

import android.util.Patterns // Import for email validation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class AuthTab {
    SIGN_UP,
    SIGN_IN
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    var selectedTab by mutableStateOf(AuthTab.SIGN_UP)
        private set

    var signUpEmail by mutableStateOf("")
        private set
    var signUpEmailError by mutableStateOf<String?>(null) // Error state
        private set

    var signUpPhone by mutableStateOf("")
        private set
    var signUpPhoneError by mutableStateOf<String?>(null) // Error state
        private set

    var signInPhone by mutableStateOf("")
        private set
    var signInPhoneError by mutableStateOf<String?>(null) // Error state
        private set

    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
        // Clear errors when switching tabs
        signUpEmailError = null
        signUpPhoneError = null
        signInPhoneError = null
    }

    fun onSignUpEmailChange(value: String) {
        signUpEmail = value
        // Clear error on change
        if (signUpEmailError != null) {
            validateSignUpEmail()
        }
    }

    fun onSignUpPhoneChange(value: String) {
        // Allow only digits and limit length (e.g., 10)
        if (value.all { it.isDigit() } && value.length <= 10) {
            signUpPhone = value
            // Clear error on change
            if (signUpPhoneError != null) {
                validateSignUpPhone()
            }
        }
    }

    fun onSignInPhoneChange(value: String) {
        // Allow only digits and limit length (e.g., 10)
        if (value.all { it.isDigit() } && value.length <= 10) {
            signInPhone = value
            // Clear error on change
            if (signInPhoneError != null) {
                validateSignInPhone()
            }
        }
    }

    // --- Validation Functions ---

    private fun validateSignUpEmail(): Boolean {
        if (signUpEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(signUpEmail).matches()) {
            signUpEmailError = "Invalid email format"
            return false
        }
        signUpEmailError = null
        return true
    }

    private fun validateSignUpPhone(): Boolean {
        if (signUpPhone.length != 10) {
            signUpPhoneError = "Must be 10 digits"
            return false
        }
        signUpPhoneError = null
        return true
    }

    private fun validateSignInPhone(): Boolean {
        if (signInPhone.length != 10) {
            signInPhoneError = "Must be 10 digits"
            return false
        }
        signInPhoneError = null
        return true
    }

    // --- Click Handlers with Validation ---

    fun onSignUpClicked(onNavigate: (String) -> Unit) {
        // Run both validations
        val isEmailValid = validateSignUpEmail()
        val isPhoneValid = validateSignUpPhone()

        if (isEmailValid && isPhoneValid) {
            // TODO: Implement actual Sign Up API call and OTP sending
            // On success, navigate to OTP screen
            onNavigate(signUpPhone)
        }
    }

    fun onSignInClicked(onNavigate: (String) -> Unit) {
        if (validateSignInPhone()) {
            // TODO: Implement actual Sign In API call and OTP sending
            // On success, navigate to OTP screen
            onNavigate(signInPhone)
        }
    }
// ...
}