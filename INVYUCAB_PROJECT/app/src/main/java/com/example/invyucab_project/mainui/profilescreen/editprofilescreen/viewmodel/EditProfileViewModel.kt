package com.example.invyucab_project.mainui.profilescreen.editprofilescreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    // State variables
    var name by mutableStateOf("")
        private set
    // ✅ REMOVED: Email State
    var gender by mutableStateOf("Male")
        private set
    var birthday by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set

    var nameError by mutableStateOf<String?>(null)
        private set
    // ✅ REMOVED: Email Error State

    init {
        loadUserAndFetchProfile()
    }

    private fun loadUserAndFetchProfile() {
        val savedPhone = userPreferences.getPhoneNumber()

        if (!savedPhone.isNullOrBlank()) {
            phone = savedPhone
            fetchUserProfile(savedPhone)
        } else {
            println("No phone number found in preferences. Cannot fetch profile.")
        }
    }

    private fun fetchUserProfile(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val response = repository.checkUser(phoneNumber)

                if (response.isSuccessful) {
                    val user = response.body()?.existingUser
                    if (user != null) {
                        name = user.fullName ?: ""
                        gender = capitalizeFirstLetter(user.gender ?: "Male")
                        birthday = formatApiDateToUi(user.dob)
                    }
                } else {
                    println("Error fetching profile: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- INPUT HANDLERS ---

    fun onNameChange(value: String) {
        val nameRegex = "^[a-zA-Z ]*$".toRegex()
        if (value.matches(nameRegex)) {
            name = value
            if (nameError != null) validateName()
        }
    }

    // ✅ REMOVED: onEmailChange

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

    // ✅ REMOVED: validateEmail

    // --- ACTIONS ---

    fun onSaveClicked(onNavigate: () -> Unit) {
        // Only validate Name now
        if (validateName()) {
            // TODO: Implement Update Profile API here
            println("Saving updated details: Name=$name, Gender=$gender, Birthday=$birthday")
            onNavigate()
        }
    }

    // --- HELPERS ---

    private fun capitalizeFirstLetter(text: String): String {
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