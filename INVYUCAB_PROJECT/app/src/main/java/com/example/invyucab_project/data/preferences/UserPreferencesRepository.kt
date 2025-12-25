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
        const val KEY_FCM_TOKEN = "fcm_token"
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

    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun saveDriverOnlineStatus(isOnline: Boolean) {
        prefs.edit().putBoolean(KEY_DRIVER_ONLINE_STATUS, isOnline).apply()
    }

    // ✅ This function effectively clears the phone number and all session data
    fun clearUserStatus() {
        prefs.edit()
            .remove(KEY_USER_STATUS)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .remove(KEY_DRIVER_ID)
            .remove(KEY_PHONE_NUMBER) // ✅ Ensure this is removed
            .remove(KEY_FCM_TOKEN)
            .remove(KEY_DRIVER_ONLINE_STATUS)
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

    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

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