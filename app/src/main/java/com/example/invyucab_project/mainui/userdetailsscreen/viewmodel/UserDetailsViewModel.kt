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

    private val rawName: String? = try {
        val encoded: String? = savedStateHandle.get<String>("name")
        encoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    } catch (e: Exception) {
        savedStateHandle.get<String>("name")
    }

    // --- UI State ---
    var name by mutableStateOf(rawName ?: "")
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
    var phoneError by mutableStateOf<String?>(null)
        private set
    var birthdayError by mutableStateOf<String?>(null)
        private set

    // --- Flags ---
    val isPhoneFromMobileAuth = rawPhone != null && rawPhone.isNotBlank()

    // --- UI Event Handlers ---

    fun onNameChange(value: String) {
        name = value
        nameError = if (value.isBlank()) "Name is required" else null
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

        return nameError == null && phoneError == null && birthdayError == null && gender.isNotBlank()
    }

    fun onSaveClicked() {
        if (!validate()) return

        val finalDob = birthday

        viewModelScope.launch {
            // ✅✅✅ START OF MODIFICATION ✅✅✅
            // This screen is now ONLY for Riders (or non-Drivers)
            // The 'else' block for Drivers has been removed.
            sendEvent(UiEvent.Navigate(
                Screen.OtpScreen.createRoute(
                    phone = phone,
                    isSignUp = true,
                    role = role,
                    name = name,
                    gender = gender,
                    dob = finalDob
                    // All driver fields will be null, which is correct for a Rider
                )
            ))
            // ✅✅✅ END OF MODIFICATION ✅✅✅
        }
    }
}