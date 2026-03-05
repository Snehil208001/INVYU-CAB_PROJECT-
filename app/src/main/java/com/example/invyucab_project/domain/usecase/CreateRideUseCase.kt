package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.CreateRideRequest
import com.example.invyucab_project.data.models.CreateRideResponse
import com.example.invyucab_project.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CreateRideUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(request: CreateRideRequest): Flow<Resource<CreateRideResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = appRepository.createRide(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response body"))
            } else {
                // ✅ FIX: Read the full error body to get the actual server error message
                val errorMsg = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }

                // Fallback to HTTP status message or code if body is empty
                val finalError = if (!errorMsg.isNullOrBlank()) {
                    errorMsg
                } else {
                    response.message().ifBlank { "Error Code: ${response.code()}" }
                }

                emit(Resource.Error(finalError))
            }
        } catch (e: HttpException) {
            // ✅ FIX: Extract error body from exception if thrown
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                null
            }
            emit(Resource.Error(errorBody ?: e.message ?: "HTTP Error"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
}