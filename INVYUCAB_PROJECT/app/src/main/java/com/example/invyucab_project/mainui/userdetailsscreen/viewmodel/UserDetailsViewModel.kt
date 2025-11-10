package com.example.invyucab_project.mainui.userdetailsscreen.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel // ✅ INHERITS FROM standard ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() { // ✅ CHANGED

    // Retrieve phone and email from navigation arguments
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

    var phone by mutableStateOf(initialPhone.orEmpty())
        private set
    var phoneError by mutableStateOf<String?>(null)
        private set

    var gender by mutableStateOf("Male")
        private set

    var birthday by mutableStateOf("")
        private set
    var birthdayError by mutableStateOf<String?>(null)
        private set

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
        if (!isEmailFromGoogle) {
            email = value
            if (emailError != null) {
                validateEmail()
            }
        }
    }

    fun onPhoneChange(value: String) {
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
        if (birthdayError != null) {
            validateBirthday()
        }
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
        if (!isEmailFromGoogle && email.isBlank()) {
            emailError = null
            return true
        }
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            return false
        }
        emailError = null
        return true
    }

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

    private fun validateBirthday(): Boolean {
        if (birthday.isBlank()) {
            birthdayError = "Date of Birth cannot be empty"
            return false
        }
        birthdayError = null
        return true
    }

    // ✅ MODIFIED: Updated the lambda to include 'name', 'gender', and 'birthday'
    fun onSaveClicked(onNavigate: (phone: String, email: String?, name: String, gender: String, birthday: String) -> Unit) {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPhoneValid = validatePhone()
        val isBirthdayValid = validateBirthday()

        if (isNameValid && isEmailValid && isPhoneValid && isBirthdayValid) {
            onNavigate(phone, email.takeIf { it.isNotBlank() }, name, gender, birthday)
        }
    }
}