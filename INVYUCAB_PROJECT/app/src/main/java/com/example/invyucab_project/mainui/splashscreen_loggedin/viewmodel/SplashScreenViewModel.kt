package com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * Checks if the user's status is "active".
     * FIX: Also validates that the User ID exists.
     */
    fun isUserLoggedIn(): Boolean {
        val status = userPreferencesRepository.getUserStatus()
        val userId = userPreferencesRepository.getUserId()

        // Strict Check: User is only logged in if status is "active" AND userId is not null
        val isValid = status == "active" && !userId.isNullOrBlank()

        if (status == "active" && !isValid) {
            // FIX: "Zombie Session" detected (Active but no ID). Force logout.
            Log.e("SplashScreen", "❌ CORRUPT STATE: User is active but has no ID. Clearing session.")
            userPreferencesRepository.clearUserStatus()
        }

        return isValid
    }

    // --- NEW FUNCTION TO GET USER ROLE ---
    /**
     * Retrieves the current user's role from preferences.
     */
    fun getUserRole(): String? {
        return userPreferencesRepository.getUserRole()
    }
    // --- END OF CHANGE ---
}