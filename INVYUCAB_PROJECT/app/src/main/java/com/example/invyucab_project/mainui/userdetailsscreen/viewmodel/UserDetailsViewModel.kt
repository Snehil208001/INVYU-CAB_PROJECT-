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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // --- State from Navigation ---
    private val rawPhone: String? = savedStateHandle.get<String>("phone")
    val role: String = savedStateHandle.get<String>("role") ?: "rider"

    // ❌ REMOVED rawEmail
    /*
    private val rawEmail: String? = try {
        val encoded: String? = savedStateHandle.get<String>("email")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("email")
    }
    */

    private val rawName: String? = try {
        val encoded: String? = savedStateHandle.get<String>("name")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("name")
    }

    // --- UI State ---
    var name by mutableStateOf(rawName ?: "")
        private set
    // var email by mutableStateOf(rawEmail ?: "") // ❌ REMOVED
    //     private set
    var phone by mutableStateOf(rawPhone ?: "")
        private set
    var gender by mutableStateOf("")
        private set
    var birthday by mutableStateOf("")
        private set

    // --- Error State ---
    var nameError by mutableStateOf<String?>(null)
        private set
    // var emailError by mutableStateOf<String?>(null) // ❌ REMOVED
    //     private set
    var phoneError by mutableStateOf<String?>(null)
        private set
    var birthdayError by mutableStateOf<String?>(null)
        private set

    // --- Flags ---
    // val isEmailFromGoogle = rawEmail != null && rawEmail.isNotBlank() // ❌ REMOVED
    val isPhoneFromMobileAuth = rawPhone != null && rawPhone.isNotBlank()

    // --- UI Event Handlers ---

    fun onNameChange(value: String) {
        name = value
        nameError = if (value.isBlank()) "Name is required" else null
    }

    // ❌ REMOVED onEmailChange function
    /*
    fun onEmailChange(value: String) {
        email = value
        emailError = if (value.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            "Invalid email format"
        } else {
            null
        }
    }
    */

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

        // ❌ REMOVED emailError check
        return nameError == null && phoneError == null && birthdayError == null && gender.isNotBlank()
    }

    fun onSaveClicked() {
        if (!validate()) return

        // val finalEmail = email.ifBlank { null } // ❌ REMOVED
        val finalDob = birthday

        viewModelScope.launch {
            if (role.equals("Rider", ignoreCase = true)) {
                // 1. If RIDER, go directly to OTP Screen
                sendEvent(UiEvent.Navigate(
                    Screen.OtpScreen.createRoute(
                        phone = phone,
                        isSignUp = true,
                        role = role,
                        // email = finalEmail, // ❌ REMOVED
                        name = name,
                        gender = gender,
                        dob = finalDob
                    )
                ))
            } else {
                // 2. If DRIVER, go to Driver Details Screen first
                sendEvent(UiEvent.Navigate(
                    Screen.DriverDetailsScreen.createRoute(
                        phone = phone,
                        role = role,
                        name = name,
                        // email = finalEmail, // ❌ REMOVED
                        gender = gender,
                        dob = finalDob
                    )
                ))
            }
        }
    }
}