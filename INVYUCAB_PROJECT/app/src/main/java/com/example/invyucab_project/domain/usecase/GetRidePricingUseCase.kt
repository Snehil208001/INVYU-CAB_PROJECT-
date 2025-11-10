package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.GetPricingRequest
import com.example.invyucab_project.domain.model.RideOption
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt
import com.example.invyucab_project.data.repository.AppRepository

/**
 * This UseCase does ONE thing: It gets pricing data and maps it to the
 * RideOption list.
 */
class GetRidePricingUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(
        pickup: LatLng,
        drop: LatLng,
        currentRideOptions: List<RideOption>
    ): Flow<Resource<List<RideOption>>> = flow {
        try {
            emit(Resource.Loading())
            val request = GetPricingRequest(
                pickupLat = pickup.latitude,
                pickupLng = pickup.longitude,
                dropLat = drop.latitude,
                dropLng = drop.longitude
            )
            val response = repository.getRidePricing(request)

            if (response.success && response.data != null) {
                val updatedOptions = currentRideOptions.map { rideOption ->
                    val priceInfo = response.data.find {
                        it.vehicle_name?.equals(rideOption.name, ignoreCase = true) == true ||
                                (it.vehicle_name?.equals("car", ignoreCase = true) == true && rideOption.name.contains("Cab"))
                    }
                    val formattedPrice = priceInfo?.let { "₹${it.total_price.roundToInt()}" } ?: "N/A"

                    rideOption.copy(
                        price = formattedPrice,
                        isLoadingPrice = false
                    )
                }
                emit(Resource.Success(updatedOptions))
            } else {
                throw Exception("API returned no price data")
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}. Prices unavailable."))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Prices unavailable."))
        } catch (e: Exception) {
            emit(Resource.Error("Could not get prices. N/A"))
        }
    }
}