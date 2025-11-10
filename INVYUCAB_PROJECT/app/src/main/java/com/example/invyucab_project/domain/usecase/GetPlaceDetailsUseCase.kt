package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It gets the LatLng for a given Place ID.
 */
class GetPlaceDetailsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(placeId: String): Flow<Resource<LatLng>> = flow {
        try {
            emit(Resource.Loading())
            val response = repository.getPlaceDetails(placeId)
            if (response.status == "OK" && response.result?.geometry?.location != null) {
                val loc = response.result.geometry.location
                emit(Resource.Success(LatLng(loc.lat, loc.lng)))
            } else {
                emit(Resource.Error("Could not get location details: ${response.status}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        }
    }
}