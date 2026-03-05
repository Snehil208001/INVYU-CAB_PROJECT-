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

    // --- State Variables (Read-Only Logic) ---
    var name by mutableStateOf("Loading...")
        private set

    var gender by mutableStateOf("")
        private set

    var birthday by mutableStateOf("")
        private set

    var phone by mutableStateOf("")
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
            name = "Guest"
            sendUiEvent("Error: No phone number found. Please log in again.")
        }
    }

    private fun fetchUserProfile(phoneNumber: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Ensure correct format for API
                val formattedPhone = if (phoneNumber.startsWith("+91")) phoneNumber else "+91$phoneNumber"
                Log.d("EditProfileVM", "Calling checkUser API for: $formattedPhone")

                val response = repository.checkUser(formattedPhone)

                if (response.isSuccessful) {
                    val user = response.body()?.existingUser
                    if (user != null) {
                        // ✅ LOG THE RAW DOB HERE TO DEBUG
                        Log.d("EditProfileVM", "User Found: ${user.fullName}, DOB Raw: '${user.dob}'")

                        name = user.fullName ?: "No Name"
                        gender = capitalizeFirstLetter(user.gender ?: "Not Specified")
                        // Format the date for UI
                        birthday = formatApiDateToUi(user.dob)
                        Log.d("EditProfileVM", "Formatted Birthday: '$birthday'")
                    } else {
                        Log.e("EditProfileVM", "Response successful but user is null")
                        name = "User Not Found"
                        sendUiEvent("User profile not found on server.")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    Log.e("EditProfileVM", "API Error: $errorMsg")
                    name = "Error Fetching Data"
                    sendUiEvent("Failed to fetch profile: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Exception", e)
                name = "Network Error"
                sendUiEvent("Network error: ${e.message}")
            } finally {
                isLoading = false
            }
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
            // Take first 10 chars (yyyy-MM-dd) to ignore time parts if present
            val datePart = if (apiDate.length >= 10) apiDate.substring(0, 10) else apiDate
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val uiFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val date = apiFormat.parse(datePart)
            if (date != null) uiFormat.format(date) else apiDate
        } catch (e: Exception) {
            // Fallback: show the raw string if parsing fails
            Log.e("EditProfileVM", "Date parsing failed for: $apiDate", e)
            apiDate
        }
    }
}