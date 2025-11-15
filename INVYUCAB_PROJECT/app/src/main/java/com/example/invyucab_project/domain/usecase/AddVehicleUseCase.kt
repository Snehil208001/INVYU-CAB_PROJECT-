package com.example.invyucab_project.domain.usecase

import android.util.Log
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.AddVehicleRequest
import com.example.invyucab_project.data.models.AddVehicleResponse
import com.example.invyucab_project.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AddVehicleUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(request: AddVehicleRequest): Flow<Resource<AddVehicleResponse>> = flow {
        try {
            emit(Resource.Loading())
            Log.d("AddVehicleUseCase", "Attempting to add vehicle...")

            val response = repository.addVehicle(request)
            Log.d("AddVehicleUseCase", "Response: $response")

            // ✅✅✅ START OF MODIFICATION ✅✅✅
            // Check the 'success' boolean from the response
            if (response.success) {
                emit(Resource.Success(response))
            } else {
                // If the server says success=false
                emit(Resource.Error("API reported failure to add vehicle"))
            }
            // ✅✅✅ END OF MODIFICATION ✅✅✅

        } catch (e: HttpException) {
            Log.e("AddVehicleUseCase", "HttpException: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e("AddVehicleUseCase", "IOException: ${e.message}", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("AddVehicleUseCase", "Exception: ${e.message}", e)
            // This is the error you were seeing (JsonDataException)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}