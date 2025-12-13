package com.example.invyucab_project.mainui.profilescreen.editprofilescreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    // --- State Variables ---
    var name by mutableStateOf("")
        private set

    var gender by mutableStateOf("Male")
        private set

    var birthday by mutableStateOf("")
        private set

    var phone by mutableStateOf("")
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Channel for one-time events like Toasts
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUserAndFetchProfile()
    }

    private fun loadUserAndFetchProfile() {
        val savedPhone = userPreferences.getPhoneNumber()
        Log.d("EditProfileVM", "Loaded Phone from Prefs: '$savedPhone'")

        if (!savedPhone.isNullOrBlank()) {
            phone = savedPhone
            fetchUserProfile(savedPhone)
        } else {
            sendUiEvent("Error: No phone number found. Please log in again.")
        }
    }

    private fun fetchUserProfile(phoneNumber: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d("EditProfileVM", "Calling checkUser API for: $phoneNumber")
                val response = repository.checkUser(phoneNumber)

                if (response.isSuccessful) {
                    val user = response.body()?.existingUser
                    if (user != null) {
                        Log.d("EditProfileVM", "User Found: ${user.fullName}")
                        name = user.fullName ?: ""
                        gender = capitalizeFirstLetter(user.gender ?: "Male")
                        birthday = formatApiDateToUi(user.dob)
                    } else {
                        Log.e("EditProfileVM", "Response successful but user is null")
                        sendUiEvent("User profile not found on server.")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    Log.e("EditProfileVM", "API Error: $errorMsg")
                    sendUiEvent("Failed to fetch profile: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Exception", e)
                sendUiEvent("Network error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // --- INPUT HANDLERS ---

    fun onNameChange(value: String) {
        // Allow letters and spaces only
        val nameRegex = "^[a-zA-Z ]*$".toRegex()
        if (value.matches(nameRegex)) {
            name = value
            if (nameError != null) validateName()
        }
    }

    fun onGenderChange(value: String) {
        gender = value
    }

    fun onBirthdayChange(value: String) {
        birthday = value
    }

    // --- VALIDATION ---

    private fun validateName(): Boolean {
        if (name.isBlank()) {
            nameError = "Name cannot be empty"
            return false
        }
        nameError = null
        return true
    }

    // --- ACTIONS ---

    fun onSaveClicked(onNavigate: () -> Unit) {
        if (validateName()) {
            // TODO: Call Update Profile API here
            sendUiEvent("Profile Updated (Local Only)")
            onNavigate()
        }
    }

    // --- HELPERS ---

    private fun sendUiEvent(message: String) {
        viewModelScope.launch {
            _uiEvent.send(message)
        }
    }

    private fun capitalizeFirstLetter(text: String): String {
        if (text.isEmpty()) return ""
        return text.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    private fun formatApiDateToUi(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return ""
        return try {
            val datePart = if (apiDate.length >= 10) apiDate.substring(0, 10) else apiDate
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val uiFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val date = apiFormat.parse(datePart)
            if (date != null) uiFormat.format(date) else apiDate
        } catch (e: Exception) {
            apiDate
        }
    }
}