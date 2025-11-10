package com.example.invyucab_project.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It logs the user out from Firebase
 * and clears their local status.
 */
class LogoutUserUseCase @Inject constructor(
    private val repository: AppRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        try {
            firebaseAuth.signOut()
            repository.clearUserStatus()
        } catch (e: Exception) {
            e.printStackTrace()
            // Error handling if needed
        }
    }
}