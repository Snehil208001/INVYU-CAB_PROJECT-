package com.example.invyucab_project.mainui.userdetailsscreen.viewmodel

import android.util.Patterns // Import for email validation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve phone and email from navigation arguments
    // ✅ MODIFIED: Get phone as nullable
    private val initialPhone: String? = savedStateHandle.get<String>("phone")
    private val initialEmail: String? = savedStateHandle.get<String>("email")
    private val initialName: String? = savedStateHandle.get<String>("name")

    // State for the text fields
    var name by mutableStateOf(initialName.orEmpty())
        private set
    var nameError by mutableStateOf<String?>(null)
        private set

    var email by mutableStateOf(initialEmail.orEmpty())
        private set
    var emailError by mutableStateOf<String?>(null)
        private set

    // ✅ MODIFIED: Phone is now mutable, pre-filled if available
    var phone by mutableStateOf(initialPhone.orEmpty())
        private set
    var phoneError by mutableStateOf<String?>(null)
        private set

    var gender by mutableStateOf("Male")
        private set
    var birthday by mutableStateOf("")
        private set

    // ✅ MODIFIED: Flags to control UI editability
    val isPhoneFromMobileAuth: Boolean = !initialPhone.isNullOrBlank()
    val isEmailFromGoogle: Boolean = !initialEmail.isNullOrBlank()

    fun onNameChange(value: String) {
        val nameRegex = "^[a-zA-Z ]*$".toRegex()
        if (value.matches(nameRegex)) {
            name = value
            if (nameError != null) {
                validateName()
            }
        }
    }

    fun onEmailChange(value: String) {
        // Only allow change if email is not from Google
        if (!isEmailFromGoogle) {
            email = value
            if (emailError != null) {
                validateEmail()
            }
        }
    }

    // ✅ ADDED: Handler for Phone change
    fun onPhoneChange(value: String) {
        // Only allow change if phone is NOT from mobile auth
        if (!isPhoneFromMobileAuth) {
            if (value.all { it.isDigit() } && value.length <= 10) {
                phone = value
                if (phoneError != null) {
                    validatePhone()
                }
            }
        }
    }


    fun onGenderChange(value: String) {
        gender = value
    }

    fun onBirthdayChange(value: String) {
        birthday = value
    }


    // --- Validation Functions ---

    private fun validateName(): Boolean {
        if (name.isBlank()) {
            nameError = "Name cannot be empty"
            return false
        }
        nameError = null
        return true
    }

    private fun validateEmail(): Boolean {
        // If email is optional (not from Google) and blank, it's valid
        if (!isEmailFromGoogle && email.isBlank()) {
            emailError = null
            return true
        }
        // If it's not blank (or from Google), it must match the pattern
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            return false
        }
        emailError = null
        return true
    }

    // ✅ ADDED: Validation for Phone
    private fun validatePhone(): Boolean {
        if (phone.isBlank()) {
            phoneError = "Phone number is required"
            return false
        }
        if (phone.length != 10) {
            phoneError = "Must be 10 digits"
            return false
        }
        phoneError = null
        return true
    }

    fun onSaveClicked(onNavigate: (phone: String, email: String?) -> Unit) {
        // MODIFIED: Run all validations
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPhoneValid = validatePhone() // Always validate phone

        if (isNameValid && isEmailValid && isPhoneValid) {
            // TODO: Implement logic to save all user details
            println("Saving user details: Name=$name, Email=$email, Phone=$phone, Gender=$gender, Birthday=$birthday")

            // Navigate to the next screen (OTP)
            // ✅ This now correctly uses the mutable 'phone' state
            onNavigate(phone, email.takeIf { it.isNotBlank() })
        }
    }
}