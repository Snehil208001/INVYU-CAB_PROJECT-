package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import com.example.invyucab_project.data.repository.AppRepository
import javax.inject.Inject

/**
 * This UseCase does ONE thing: It checks if a user exists.
 * It returns a Flow that emits Loading, then Success or Error.
 */
class CheckUserUseCase @Inject constructor(
    private val repository: AppRepository
) {

    // ✅ FIX 1: The return type is correct (UserCheckStatus)
    operator fun invoke(phoneNumber: String): Flow<Resource<UserCheckStatus>> = flow {
        try {
            emit(Resource.Loading()) // 1. Emit loading

            val fullPhone = "+91$phoneNumber"
            val response = repository.checkUser(fullPhone)

            if (response.isSuccessful && response.body()?.existingUser != null) {
                // ✅ FIX 2: Get the role AND userId from the response
                val user = response.body()!!.existingUser!!
                val role = user.userRole ?: "rider"
                val userId = user.userId // <-- This is the missing ID

                // ✅ FIX 3: Emit success WITH the user's role AND userId
                emit(Resource.Success(UserCheckStatus.Exists(role, userId)))
            } else {
                emit(Resource.Success(UserCheckStatus.DoesNotExist))
            }

        } catch (e: HttpException) {
            if (e.code() == 404) {
                emit(Resource.Success(UserCheckStatus.DoesNotExist))
            } else {
                emit(Resource.Error("Server error: ${e.message()}. Please try again."))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred."))
        }
    }
}

// ✅ FIX 4: Change this from an enum to a sealed interface
// This allows us to pass data (the role) in the "Exists" state
sealed interface UserCheckStatus {
    // ✅ FIX 5: Add the userId to the Exists data class
    data class Exists(val role: String, val userId: Int) : UserCheckStatus
    object DoesNotExist : UserCheckStatus
}