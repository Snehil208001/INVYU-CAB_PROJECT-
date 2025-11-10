package com.example.invyucab_project.mainui.userdetailsscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder // ✅ ADDED
import java.nio.charset.StandardCharsets // ✅ ADDED
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // --- State from Navigation ---
    private val rawPhone: String? = savedStateHandle.get<String>("phone")

    // ✅ ADDED: Get role from navigation
    val role: String = savedStateHandle.get<String>("role") ?: "rider"

    // ✅ MODIFIED: Handle decoding for email/name
    private val rawEmail: String? = try {
        val encoded: String? = savedStateHandle.get<String>("email")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("email")
    }

    private val rawName: String? = try {
        val encoded: String? = savedStateHandle.get<String>("name")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("name")
    }

    // --- UI State ---
    var name by mutableStateOf(rawName ?: "")
        private set
    var email by mutableStateOf(rawEmail ?: "")
        private set
    var phone by mutableStateOf(rawPhone ?: "")
        private set
    var gender by mutableStateOf("")
        private set
    var birthday by mutableStateOf("")
        private set

    // --- Error State ---
    var nameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var phoneError by mutableStateOf<String?>(null)
        private set
    var birthdayError by mutableStateOf<String?>(null)
        private set

    // --- Flags ---
    val isEmailFromGoogle = rawEmail != null && rawEmail.isNotBlank()
    val isPhoneFromMobileAuth = rawPhone != null && rawPhone.isNotBlank()

    // --- UI Event Handlers ---

    fun onNameChange(value: String) {
        name = value
        nameError = if (value.isBlank()) "Name is required" else null
    }

    fun onEmailChange(value: String) {
        email = value
        // Basic email validation (optional)
        emailError = if (value.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            "Invalid email format"
        } else {
            null
        }
    }

    fun onPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            phone = value
            phoneError = if (value.length != 10) "Must be 10 digits" else null
        }
    }

    fun onGenderChange(value: String) {
        gender = value
    }

    fun onBirthdayChange(value: String) {
        birthday = value
        birthdayError = if (value.isBlank()) "Date of birth is required" else null
    }

    private fun validate(): Boolean {
        nameError = if (name.isBlank()) "Name is required" else null
        phoneError = if (phone.length != 10) "Must be 10 digits" else null
        birthdayError = if (birthday.isBlank()) "Date of birth is required" else null
        // Gender is validated by dialog

        return nameError == null && phoneError == null && birthdayError == null && gender.isNotBlank()
    }

    // ✅✅✅ START OF CHANGE ✅✅✅
    // ViewModel now handles navigation logic
    fun onSaveClicked() {
        if (!validate()) return

        // Clear focus (optional, good practice)
        // ... (requires keyboard controller)

        val finalEmail = email.ifBlank { null }
        val finalDob = birthday // Already in "MMMM d, yyyy" format

        viewModelScope.launch {
            if (role.equals("Rider", ignoreCase = true)) {
                // 1. If RIDER, go directly to OTP Screen
                sendEvent(UiEvent.Navigate(
                    Screen.OtpScreen.createRoute(
                        phone = phone,
                        isSignUp = true,
                        role = role,
                        email = finalEmail,
                        name = name,
                        gender = gender,
                        dob = finalDob
                        // Driver fields (license, etc.) are null by default
                    )
                ))
            } else {
                // 2. If DRIVER, go to Driver Details Screen first
                sendEvent(UiEvent.Navigate(
                    Screen.DriverDetailsScreen.createRoute(
                        phone = phone,
                        role = role,
                        name = name,
                        email = finalEmail,
                        gender = gender,
                        dob = finalDob
                    )
                ))
            }
        }
    }
    // ✅✅✅ END OF CHANGE ✅✅✅
}