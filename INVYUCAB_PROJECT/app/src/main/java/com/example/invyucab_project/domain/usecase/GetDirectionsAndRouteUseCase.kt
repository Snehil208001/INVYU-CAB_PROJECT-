package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.DirectionsResponse
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import com.example.invyucab_project.data.repository.AppRepository
import javax.inject.Inject

/**
 * This UseCase does ONE thing: It gets the directions and decodes the polyline.
 * It returns a custom data class with all the info the
 * RideSelectionViewModel needs.
 */
class GetDirectionsAndRouteUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(origin: LatLng, destinationPlaceId: String): Flow<Resource<RouteInfo>> = flow {
        try {
            emit(Resource.Loading())
            val originString = "${origin.latitude},${origin.longitude}"
            val destinationString = "place_id:$destinationPlaceId"

            val response = repository.getDirections(originString, destinationString)

            if (response.status == "OK" && response.routes.isNotEmpty()) {
                val route = response.routes[0]
                val leg = route.legs.firstOrNull()

                val routeInfo = RouteInfo(
                    polyline = PolyUtil.decode(route.overviewPolyline.points),
                    durationSeconds = leg?.duration?.value,
                    distanceMeters = leg?.distance?.value
                )
                emit(Resource.Success(routeInfo))
            } else {
                emit(Resource.Error("Could not get directions: ${response.status}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        }
    }
}

data class RouteInfo(
    val polyline: List<LatLng>,
    val durationSeconds: Int?,
    val distanceMeters: Int?
)