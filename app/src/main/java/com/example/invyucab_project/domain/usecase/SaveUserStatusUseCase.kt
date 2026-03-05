package com.example.invyucab_project.domain.usecase

import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It saves the user's status to local preferences.
 * This is a "fire and forget" operation, so it doesn't return a Flow.
 */
class SaveUserStatusUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(status: String) {
        try {
            repository.saveUserStatus(status)
        } catch (e: Exception) {
            e.printStackTrace()
            // We could return a Result.failure(e) if we needed to
        }
    }
}