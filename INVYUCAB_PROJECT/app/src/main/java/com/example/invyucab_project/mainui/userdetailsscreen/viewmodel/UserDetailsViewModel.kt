package com.example.invyucab_project.mainui.userdetailsscreen.viewmodel

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

    // State for the text fields
    var name by mutableStateOf("")
        private set

    // Pre-fill email if it exists, but allow user to change it
    var email by mutableStateOf(initialEmail.orEmpty())
        private set

    fun onNameChange(value: String) {
        name = value
    }

    fun onEmailChange(value: String) {
        email = value
    }

    fun onSaveClicked(onNavigate: () -> Unit) {
        // TODO: Implement logic to save the user's name and email to your backend/database
        println("Saving user details: Name=$name, Email=$email, Phone=$phone")

        // Navigate to the next screen (e.g., Home)
        onNavigate()
    }
}