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
            emit(Resource.Loading()) // 1. Emit loading

            val fullPhone = "+91$phoneNumber"
            val response = repository.checkUser(fullPhone)

            if (response.isSuccessful && response.body()?.existingUser != null) {
                emit(Resource.Success(UserCheckStatus.EXISTS)) // 2. Emit success
            } else {
                emit(Resource.Success(UserCheckStatus.DOES_NOT_EXIST)) // 2. Emit success
            }

        } catch (e: HttpException) {
            if (e.code() == 404) {
                // 404 is an expected "success" for sign-up
                emit(Resource.Success(UserCheckStatus.DOES_NOT_EXIST))
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

enum class UserCheckStatus {
    EXISTS,
    DOES_NOT_EXIST
}