package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.data.repository.AppRepository
import retrofit2.HttpException
import javax.inject.Inject

/**
 * This UseCase does ONE thing: It checks if a user exists.
 * It contains the business logic for that check, such as:
 * - Prepending "+91" to the phone number.
 * - Interpreting what a 404 error means (in this case, "Does Not Exist").
 */
class CheckUserUseCase @Inject constructor(
    private val repository: AppRepository
) {

    /**
     * The 'invoke' operator lets us call this class as if it's a function.
     * It returns a Kotlin `Result` object, which cleanly wraps
     * success (with a `UserCheckStatus`) or failure (with an `Exception`).
     */
    suspend operator fun invoke(phoneNumber: String): Result<UserCheckStatus> {
        return try {
            // Business logic: Add country code
            val fullPhone = "+91$phoneNumber"

            // Call the repository
            val response = repository.checkUser(fullPhone)

            // Business logic: Interpret the response
            if (response.isSuccessful && response.body()?.existingUser != null) {
                Result.success(UserCheckStatus.EXISTS)
            } else {
                Result.success(UserCheckStatus.DOES_NOT_EXIST)
            }

        } catch (e: HttpException) {
            // Business logic: A 404 error from this API means the user does not exist.
            if (e.code() == 404) {
                Result.success(UserCheckStatus.DOES_NOT_EXIST)
            } else {
                // Any other HTTP error is a real failure.
                Result.failure(e)
            }
        } catch (e: Exception) {
            // General network errors, etc.
            Result.failure(e)
        }
    }
}

/**
 * A simple, clear enum to represent the result of this UseCase.
 */
enum class UserCheckStatus {
    EXISTS,
    DOES_NOT_EXIST
}