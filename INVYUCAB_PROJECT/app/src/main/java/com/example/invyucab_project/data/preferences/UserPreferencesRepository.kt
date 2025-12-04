package com.example.invyucab_project.data.preferences

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        const val KEY_USER_STATUS = "user_status"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_DRIVER_ID = "driver_id"
        // ✅ ADDED: Key for storing Phone Number
        const val KEY_PHONE_NUMBER = "phone_number"
    }

    fun saveUserStatus(status: String) {
        prefs.edit().putString(KEY_USER_STATUS, status).apply()
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun saveDriverId(driverId: String) {
        prefs.edit().putString(KEY_DRIVER_ID, driverId).apply()
    }

    // ✅ ADDED: Function to save phone number
    fun savePhoneNumber(phoneNumber: String) {
        prefs.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply()
    }

    /**
     * Clears the user's status and data from SharedPreferences (e.g., on logout).
     */
    fun clearUserStatus() {
        prefs.edit()
            .remove(KEY_USER_STATUS)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .remove(KEY_DRIVER_ID)
            .remove(KEY_PHONE_NUMBER) // ✅ Remove phone number on logout
            .apply()
    }

    fun getUserStatus(): String? {
        return prefs.getString(KEY_USER_STATUS, null)
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun getDriverId(): String? {
        return prefs.getString(KEY_DRIVER_ID, null)
    }

    // ✅ ADDED: Function to get phone number
    fun getPhoneNumber(): String? {
        return prefs.getString(KEY_PHONE_NUMBER, null)
    }

    fun saveOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}