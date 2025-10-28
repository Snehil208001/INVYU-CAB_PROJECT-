package com.example.invyucab_project.mainui.profilescreen.editprofilescreen.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    // In a real app, you'd inject a Repository to get/update user data
) : ViewModel() {

    // Pre-populate with data. In a real app, this would be fetched
    var name by mutableStateOf("Snehil")
        private set
    var email by mutableStateOf("")
        private set
    var gender by mutableStateOf("Male") // ✅ ADDED
        private set
    var birthday by mutableStateOf("April 16, 1988") // ✅ ADDED
        private set
    var phone by mutableStateOf("+91 7542957884")
        private set

    var nameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set

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
        email = value
        if (emailError != null) {
            validateEmail()
        }
    }

    // ✅ ADDED
    fun onGenderChange(value: String) {
        gender = value
    }

    // ✅ ADDED
    fun onBirthdayChange(value: String) {
        birthday = value
        // TODO: Add date validation if needed
    }

    private fun validateName(): Boolean {
        if (name.isBlank()) {
            nameError = "Name cannot be empty"
            return false
        }
        nameError = null
        return true
    }

    private fun validateEmail(): Boolean {
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            return false
        }
        emailError = null
        return true
    }

    fun onSaveClicked(onNavigate: () -> Unit) {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()

        if (isNameValid && isEmailValid) {
            // TODO: Implement logic to save updated details to your backend
            println("Saving updated details: Name=$name, Email=$email, Gender=$gender, Birthday=$birthday")

            // Navigate back after saving
            onNavigate()
        }
    }
}