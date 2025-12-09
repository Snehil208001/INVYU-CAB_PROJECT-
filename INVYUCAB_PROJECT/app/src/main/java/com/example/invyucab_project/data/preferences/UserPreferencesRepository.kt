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
        const val KEY_PHONE_NUMBER = "phone_number"
        // ✅ ADDED: Key for FCM Token
        const val KEY_FCM_TOKEN = "fcm_token"
        // ✅ ADDED: Key for Driver Online Status
        const val KEY_DRIVER_ONLINE_STATUS = "driver_online_status"
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

    fun savePhoneNumber(phoneNumber: String) {
        prefs.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply()
    }

    // ✅ ADDED: Function to save FCM token
    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    // ✅ ADDED: Function to save Driver Online Status
    fun saveDriverOnlineStatus(isOnline: Boolean) {
        prefs.edit().putBoolean(KEY_DRIVER_ONLINE_STATUS, isOnline).apply()
    }

    fun clearUserStatus() {
        prefs.edit()
            .remove(KEY_USER_STATUS)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .remove(KEY_DRIVER_ID)
            .remove(KEY_PHONE_NUMBER)
            .remove(KEY_FCM_TOKEN) // ✅ Clear token on logout
            .remove(KEY_DRIVER_ONLINE_STATUS) // ✅ Clear online status on logout
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

    fun getPhoneNumber(): String? {
        return prefs.getString(KEY_PHONE_NUMBER, null)
    }

    // ✅ ADDED: Function to get FCM token
    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    // ✅ ADDED: Function to get Driver Online Status (Default is false)
    fun getDriverOnlineStatus(): Boolean {
        return prefs.getBoolean(KEY_DRIVER_ONLINE_STATUS, false)
    }

    fun saveOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}