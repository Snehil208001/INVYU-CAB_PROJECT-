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
    val phone: String = savedStateHandle.get<String>("phone") ?: ""
    private val initialEmail: String? = savedStateHandle.get<String>("email")
    // ✅ ADDED: Retrieve initial name from navigation arguments
    private val initialName: String? = savedStateHandle.get<String>("name")

    // State for the text fields
    // ✅ MODIFIED: Pre-fill name if it exists
    var name by mutableStateOf(initialName.orEmpty())
        private set
    var nameError by mutableStateOf<String?>(null) // ADDED: Error state
        private set

    // Pre-fill email if it exists, but allow user to change it
    var email by mutableStateOf(initialEmail.orEmpty())
        private set
    var emailError by mutableStateOf<String?>(null) // ADDED: Error state
        private set

    fun onNameChange(value: String) {
        // MODIFIED: Regex to allow only letters (a-z, A-Z) and spaces
        val nameRegex = "^[a-zA-Z ]*$".toRegex()

        // Only update the state if the new value matches the regex
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

    // --- ADDED: Validation Functions ---

    private fun validateName(): Boolean {
        // isBlank() checks for empty string OR just whitespace
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
        // MODIFIED: Run validations before navigating
        val isNameValid = validateName()
        val isEmailValid = validateEmail()

        if (isNameValid && isEmailValid) {
            // TODO: Implement logic to save the user's name and email to your backend/database
            println("Saving user details: Name=$name, Email=$email, Phone=$phone")

            // Navigate to the next screen (e.g., Home)
            onNavigate()
        }
    }
}