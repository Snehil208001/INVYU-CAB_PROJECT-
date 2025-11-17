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
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // ✅ --- ADDED KEY FOR USER ID ---
        const val KEY_USER_ID = "user_id"
        // ✅ --- END OF CHANGE ---

        // ✅ --- ADDED KEY FOR USER ROLE ---
        const val KEY_USER_ROLE = "user_role"
        // ✅ --- END OF CHANGE ---

        // ✅ --- ADDED KEY FOR DRIVER ID ---
        const val KEY_DRIVER_ID = "driver_id"
        // ✅ --- END OF CHANGE ---
    }

    /**
     * Saves the user's status (e.g., "active") to SharedPreferences.
     */
    fun saveUserStatus(status: String) {
        prefs.edit().putString(KEY_USER_STATUS, status).apply()
    }

    // ✅ --- NEW FUNCTION TO SAVE USER ID ---
    /**
     * Saves the user's unique ID (as a String) to SharedPreferences.
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    // ✅ --- END OF CHANGE ---

    // ✅ --- NEW FUNCTION TO SAVE USER ROLE ---
    /**
     * Saves the user's role (e.g., "User", "Driver") to SharedPreferences.
     */
    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }
    // ✅ --- END OF CHANGE ---

    // ✅ --- NEW FUNCTION TO SAVE DRIVER ID ---
    /**
     * Saves the driver's unique ID (as a String) to SharedPreferences.
     */
    fun saveDriverId(driverId: String) {
        prefs.edit().putString(KEY_DRIVER_ID, driverId).apply()
    }
    // ✅ --- END OF CHANGE ---

    /**
     * Clears the user's status from SharedPreferences (e.g., on logout).
     */
    fun clearUserStatus() {
        prefs.edit()
            .remove(KEY_USER_STATUS)
            .remove(KEY_USER_ID) // ✅ Also remove user ID on logout
            .remove(KEY_USER_ROLE) // ✅ Also remove user role on logout
            .remove(KEY_DRIVER_ID) // ✅ Also remove driver ID on logout
            .apply()
    }

    /**
     * Retrieves the current user status.
     * Returns null if no status is saved.
     */
    fun getUserStatus(): String? {
        return prefs.getString(KEY_USER_STATUS, null)
    }

    // ✅ --- NEW FUNCTION TO GET USER ID ---
    /**
     * Retrieves the current user's ID.
     * Returns null if no ID is saved.
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    // ✅ --- END OF CHANGE ---

    // ✅ --- NEW FUNCTION TO GET USER ROLE ---
    /**
     * Retrieves the current user's role.
     * Returns null if no role is saved.
     */
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }
    // ✅ --- END OF CHANGE ---

    // ✅ --- NEW FUNCTION TO GET DRIVER ID ---
    /**
     * Retrieves the current driver's ID.
     * Returns null if no ID is saved.
     */
    fun getDriverId(): String? {
        return prefs.getString(KEY_DRIVER_ID, null)
    }
    // ✅ --- END OF CHANGE ---

    /**
     * Saves a flag indicating the user has seen the onboarding flow.
     */
    fun saveOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    /**
     * Checks if the user has completed the onboarding flow.
     * @return true if completed, false otherwise.
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}