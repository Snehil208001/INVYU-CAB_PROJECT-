package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * This UseCase does ONE thing: It logs the user out from Firebase
 * and clears their local status directly from preferences.
 */
class LogoutUserUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository, // ✅ Use Preferences Repo directly
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        try {
            // 1. Sign out from Firebase
            firebaseAuth.signOut()

            // 2. Clear all stored local data (User ID, Phone, Role, etc.)
            userPreferencesRepository.clearUserStatus()

        } catch (e: Exception) {
            e.printStackTrace()
            // Error handling if needed
        }
    }
}