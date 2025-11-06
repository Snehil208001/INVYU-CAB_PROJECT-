package com.example.invyucab_project.data.preferences

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        // Key for storing the user's status
        const val KEY_USER_STATUS = "user_status"
        // ✅ ADDED: Key for onboarding
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    /**
     * Saves the user's status (e.g., "active") to SharedPreferences.
     */
    fun saveUserStatus(status: String) {
        prefs.edit().putString(KEY_USER_STATUS, status).apply()
    }

    /**
     * Clears the user's status from SharedPreferences (e.g., on logout).
     */
    fun clearUserStatus() {
        prefs.edit().remove(KEY_USER_STATUS).apply()
    }

    /**
     * Retrieves the current user status.
     * Returns null if no status is saved.
     */
    fun getUserStatus(): String? {
        return prefs.getString(KEY_USER_STATUS, null)
    }

    // ✅✅✅ NEW FUNCTION ✅✅✅
    /**
     * Saves a flag indicating the user has seen the onboarding flow.
     */
    fun saveOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    // ✅✅✅ NEW FUNCTION ✅✅✅
    /**
     * Checks if the user has completed the onboarding flow.
     * @return true if completed, false otherwise.
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}