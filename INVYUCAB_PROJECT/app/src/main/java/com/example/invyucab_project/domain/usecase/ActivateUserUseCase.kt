package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.UpdateUserStatusRequest
import com.example.invyucab_project.data.models.UpdateUserStatusResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It activates a user's status in the backend during Sign-In.
 */
class ActivateUserUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(phoneNumber: String, email: String?): Flow<Resource<UpdateUserStatusResponse>> = flow {
        try {
            emit(Resource.Loading())
            val request = UpdateUserStatusRequest(
                phoneNumber = "+91$phoneNumber",
                status = "active",
                email = email
            )
            val response = repository.updateUserStatus(request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        }
    }
}