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
    // ✅ FIXED: Changed parameter name to 'destination' and removed "place_id:" prefix.
    // This allows passing either "place_id:XYZ" OR "lat,lng" from the ViewModel.
    operator fun invoke(origin: LatLng, destination: String): Flow<Resource<RouteInfo>> = flow {
        fetchRoute(
            originString = "${origin.latitude},${origin.longitude}",
            destinationString = destination
        )
    }

    // Overload for Coordinates (For RideTrackingScreen)
    operator fun invoke(origin: LatLng, destination: LatLng): Flow<Resource<RouteInfo>> = flow {
        fetchRoute(
            originString = "${origin.latitude},${origin.longitude}",
            destinationString = "${destination.latitude},${destination.longitude}"
        )
    }

    // Helper logic
    private suspend fun kotlinx.coroutines.flow.FlowCollector<Resource<RouteInfo>>.fetchRoute(
        originString: String,
        destinationString: String
    ) {
        try {
            emit(Resource.Loading())
            Log.d("DirectionsUC", "🌍 Req: $originString -> $destinationString")

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
                emit(Resource.Error("API Error: ${response.status}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Check connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }
}

data class RouteInfo(
    val polyline: List<LatLng>,
    val durationSeconds: Int?,
    val distanceMeters: Int?
)