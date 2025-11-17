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
                } ?: throw IOException("Empty response body")
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message()))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
}