package com.example.invyucab_project.domain.usecase

import android.util.Log
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.VehicleDetails
import com.example.invyucab_project.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * This UseCase does ONE thing: It gets the driver's vehicle details.
 * It returns the FIRST VehicleDetails if found, or null if not found.
 */
class GetVehicleDetailsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(driverId: String): Flow<Resource<VehicleDetails?>> = flow {
        try {
            emit(Resource.Loading())
            Log.d("GetVehicleDetailsUC", "Fetching vehicle for driverId: $driverId")
            val response = repository.getVehicleDetails(driverId)

            if (response.isSuccessful) {
                // Success: The API responded.
                val responseBody = response.body()

                // ✅✅✅ START OF FIX ✅✅✅
                // Check for a valid, non-empty data list
                val vehicleList = responseBody?.data
                val firstVehicle = if (vehicleList.isNullOrEmpty()) {
                    null // No vehicles found
                } else {
                    vehicleList[0] // Get the first vehicle
                }

                if (firstVehicle != null) {
                    Log.d("GetVehicleDetailsUC", "Vehicle found: ${firstVehicle.vehicleNumber}")
                    emit(Resource.Success(firstVehicle))
                } else {
                    // This handles "No vehicle found" or empty data list
                    Log.d("GetVehicleDetailsUC", "No vehicle found (list is null or empty, or error in body).")
                    emit(Resource.Success(null))
                }
                // ✅✅✅ END OF FIX ✅✅✅

            } else {
                // Server error (4xx, 5xx)
                Log.e("GetVehicleDetailsUC", "Server error: ${response.code()} ${response.message()}")
                emit(Resource.Error("Server error: ${response.code()} ${response.message()}"))
            }
        } catch (e: HttpException) {
            Log.e("GetVehicleDetailsUC", "HttpException: ${e.message()}", e)
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("GetVehicleDetailsUC", "IOException: ${e.message}", e)
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            // This will catch the Moshi parsing errors if they still happen
            Log.e("GetVehicleDetailsUC", "Exception: ${e.message}", e)
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}