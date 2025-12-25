package com.example.invyucab_project.mainui.ridehistoryscreen.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.GetRideHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

// ✅ ADDED: UI Model with all necessary fields (including rating/vehicle)
data class TravelHistoryUiModel(
    val rideId: Int,
    val riderId: Int?,
    val driverId: Int?,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropLat: Double,
    val dropLng: Double,
    val pickupAddress: String,
    val dropAddress: String,
    val estimatedPrice: String?,
    val actualPrice: String?,
    val status: String?,
    val requestedAt: String?,
    val startedAt: String?,
    val driverName: String?,
    val model: String?,
    val vehicleNumber: String?, // ✅ Added
    val driverRating: String?   // ✅ Added
)

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val getRideHistoryUseCase: GetRideHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _rideHistory = MutableStateFlow<List<TravelHistoryUiModel>>(emptyList())
    val rideHistory: StateFlow<List<TravelHistoryUiModel>> = _rideHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchRideHistory()
    }

    fun fetchRideHistory() {
        val userIdStr = userPreferencesRepository.getUserId()
        val userId = userIdStr?.toIntOrNull()

        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = getRideHistoryUseCase.invoke(userId = userId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val rawHistory = response.body()?.data ?: emptyList()

                    val mappedHistory = rawHistory.map { item ->
                        val pLat = item.pickupLatitude?.toDoubleOrNull() ?: 0.0
                        val pLng = item.pickupLongitude?.toDoubleOrNull() ?: 0.0
                        val dLat = item.dropLatitude?.toDoubleOrNull() ?: 0.0
                        val dLng = item.dropLongitude?.toDoubleOrNull() ?: 0.0

                        // Convert Coords to Address
                        val pAddr = getAddressFromCoordinates(pLat, pLng)
                        val dAddr = getAddressFromCoordinates(dLat, dLng)

                        TravelHistoryUiModel(
                            rideId = item.rideId,
                            riderId = item.riderId,
                            driverId = item.driverId,
                            pickupLat = pLat,
                            pickupLng = pLng,
                            dropLat = dLat,
                            dropLng = dLng,
                            pickupAddress = pAddr,
                            dropAddress = dAddr,
                            estimatedPrice = item.estimatedPrice,
                            actualPrice = item.actualPrice,
                            status = item.status,
                            requestedAt = item.requestedAt,
                            startedAt = item.startedAt,
                            driverName = item.driverName,
                            model = item.model,
                            vehicleNumber = item.vehicleNumber, // ✅ Mapped
                            driverRating = item.driverRating    // ✅ Mapped
                        )
                    }

                    _rideHistory.value = mappedHistory.reversed()
                } else {
                    _error.value = "Failed to load ride history"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("RideHistoryVM", "Error fetching history", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        if (lat == 0.0 || lng == 0.0) return "Invalid Location"
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Lat: $lat, Lng: $lng"
                } else {
                    "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
                }
            } catch (e: Exception) {
                "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
            }
        }
    }
}