package com.example.invyucab_project.domain.usecase

import android.util.Log
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.repository.AppRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetDirectionsAndRouteUseCase @Inject constructor(
    private val repository: AppRepository
) {
    // Existing function for Place ID (Keep this for RideSelectionScreen)
    operator fun invoke(origin: LatLng, destinationPlaceId: String): Flow<Resource<RouteInfo>> = flow {
        fetchRoute(
            originString = "${origin.latitude},${origin.longitude}",
            destinationString = "place_id:$destinationPlaceId"
        )
    }

    // ✅ NEW: Overload for Coordinates (For RideTrackingScreen)
    operator fun invoke(origin: LatLng, destination: LatLng): Flow<Resource<RouteInfo>> = flow {
        val originString = "${origin.latitude},${origin.longitude}"
        val destString = "${destination.latitude},${destination.longitude}"

        // Reuse the logic by delegating to the helper (or copying the body if you prefer)
        try {
            emit(Resource.Loading())
            Log.d("DirectionsUC", "🌍 Req: $originString -> $destString")

            val response = repository.getDirections(originString, destString)

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
                emit(Resource.Error("API Error: ${response.status}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    // Helper to avoid code duplication (Optional, or just copy the try-catch block above)
    private suspend fun kotlinx.coroutines.flow.FlowCollector<Resource<RouteInfo>>.fetchRoute(
        originString: String,
        destinationString: String
    ) {
        try {
            emit(Resource.Loading())
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
                emit(Resource.Error("Error: ${response.status}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error"))
        }
    }
}

data class RouteInfo(
    val polyline: List<LatLng>,
    val durationSeconds: Int?,
    val distanceMeters: Int?
)