package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It gets location autocomplete predictions.
 */
class GetAutocompletePredictionsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(query: String, sessionToken: String): Flow<Resource<List<Prediction>>> = flow {
        try {
            emit(Resource.Loading())
            val response = repository.getPlaceAutocomplete(query, sessionToken)
            if (response.status == "OK") {
                emit(Resource.Success(response.predictions))
            } else {
                emit(Resource.Error("Could not fetch locations: ${response.status}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        }
    }
}