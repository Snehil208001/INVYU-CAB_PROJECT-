package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.data.models.CreateUserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It creates a new user.
 */
class CreateUserUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(request: CreateUserRequest): Flow<Resource<CreateUserResponse>> = flow {
        try {
            emit(Resource.Loading())

            // This API returns the object directly or throws an exception
            val userResponse = repository.createUser(request)

            emit(Resource.Success(userResponse))
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}. Please try again."))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred."))
        }
    }
}