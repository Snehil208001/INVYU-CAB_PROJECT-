package com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel

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
     */
    fun isUserLoggedIn(): Boolean {
        return userPreferencesRepository.getUserStatus() == "active"
    }

    // ✅ --- NEW FUNCTION TO GET USER ROLE ---
    /**
     * Retrieves the current user's role from preferences.
     */
    fun getUserRole(): String? {
        return userPreferencesRepository.getUserRole()
    }
    // ✅ --- END OF CHANGE ---
}