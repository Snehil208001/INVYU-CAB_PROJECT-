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

    operator fun invoke(phoneNumber: String): Flow<Resource<UserCheckStatus>> = flow {
        try {
            emit(Resource.Loading())

            // Ensure we handle cases where input might already have +91 to avoid double prefix
            val cleanPhone = if (phoneNumber.startsWith("+91")) phoneNumber.substring(3) else phoneNumber
            val fullPhone = "+91$cleanPhone"

            val response = repository.checkUser(fullPhone)

            if (response.isSuccessful && response.body()?.existingUser != null) {
                val user = response.body()!!.existingUser!!
                val role = user.userRole ?: "rider"
                val userId = user.userId
                // ✅ CAPTURE THE NAME FROM API
                val name = user.fullName ?: "User"
                // ✅ CAPTURE THE PHONE NUMBER FROM API
                val phone = user.phoneNumber

                // ✅ PASS NAME AND PHONE TO THE STATE
                emit(Resource.Success(UserCheckStatus.Exists(role, userId, name, phone)))
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

sealed interface UserCheckStatus {
    // ✅ ADDED name and phoneNumber parameter
    data class Exists(val role: String, val userId: Int, val name: String, val phoneNumber: String) : UserCheckStatus
    object DoesNotExist : UserCheckStatus
}