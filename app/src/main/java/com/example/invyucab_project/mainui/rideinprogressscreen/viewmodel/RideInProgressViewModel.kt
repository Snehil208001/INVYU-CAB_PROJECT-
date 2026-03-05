package com.example.invyucab_project.mainui.rideinprogressscreen.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RideInProgressViewModel @Inject constructor(
    private val repository: AppRepository,
    private val getOngoingRideUseCase: GetOngoingRideUseCase,
    @ApplicationContext private val context: Context // Injected for Geocoding
) : ViewModel() {

    private val _updateStatus = MutableStateFlow<Result<Unit>?>(null)
    val updateStatus: StateFlow<Result<Unit>?> = _updateStatus

    private val _rideState = MutableStateFlow<Resource<RiderOngoingRideResponse>>(Resource.Loading())
    val rideState: StateFlow<Resource<RiderOngoingRideResponse>> = _rideState.asStateFlow()

    // SharedFlow to trigger navigation from ViewModel
    private val _navigateToBill = MutableSharedFlow<String>()
    val navigateToBill = _navigateToBill.asSharedFlow()

    fun fetchOngoingRide(rideId: Int) {
        viewModelScope.launch {
            try {
                val response = getOngoingRideUseCase(rideId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _rideState.value = Resource.Success(body)

                    // Check if ride is completed (Rider Side Logic)
                    val ride = body.data?.firstOrNull()
                    if (ride?.status == "completed") {
                        val fare = ride.totalAmount ?: ride.price ?: ride.estimatedPrice ?: "0.0"

                        // Get coords
                        val pLat = ride.pickupLatitude?.toDoubleOrNull()
                        val pLng = ride.pickupLongitude?.toDoubleOrNull()
                        val dLat = ride.dropLatitude?.toDoubleOrNull()
                        val dLng = ride.dropLongitude?.toDoubleOrNull()

                        // Use API address if available, otherwise Reverse Geocode
                        val pAddress = ride.pickupAddress ?: getAddressFromLatLng(pLat, pLng) ?: "Unknown Pickup"
                        val dAddress = ride.dropAddress ?: getAddressFromLatLng(dLat, dLng) ?: "Unknown Drop"

                        emitBillNavigation(fare.toString(), "rider", pAddress, dAddress)
                    }

                } else {
                    _rideState.value = Resource.Error(response.message() ?: "Failed to fetch ride")
                }
            } catch (e: Exception) {
                _rideState.value = Resource.Error(e.message ?: "Error loading ride")
            }
        }
    }

    fun updateRideStatus(rideId: Int, status: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateRideStatus(rideId, status)
                if (response.isSuccessful && response.body()?.success == true) {
                    _updateStatus.value = Result.success(Unit)

                    // If Driver completes the ride (Driver Side Logic)
                    if (status == "completed") {
                        // Use the data currently held in _rideState
                        val ride = _rideState.value.data?.data?.firstOrNull()
                        val fare = ride?.totalAmount ?: ride?.price ?: ride?.estimatedPrice ?: "0.0"

                        val pLat = ride?.pickupLatitude?.toDoubleOrNull()
                        val pLng = ride?.pickupLongitude?.toDoubleOrNull()
                        val dLat = ride?.dropLatitude?.toDoubleOrNull()
                        val dLng = ride?.dropLongitude?.toDoubleOrNull()

                        val pAddress = ride?.pickupAddress ?: getAddressFromLatLng(pLat, pLng) ?: "Pickup Location"
                        val dAddress = ride?.dropAddress ?: getAddressFromLatLng(dLat, dLng) ?: "Drop Location"

                        emitBillNavigation(fare.toString(), "driver", pAddress, dAddress)
                    }
                } else {
                    _updateStatus.value = Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                _updateStatus.value = Result.failure(e)
            }
        }
    }

    // Helper function to build the route string safely
    private suspend fun emitBillNavigation(fare: String, role: String, pickup: String, drop: String) {
        val encodedPickup = URLEncoder.encode(pickup, StandardCharsets.UTF_8.toString())
        val encodedDrop = URLEncoder.encode(drop, StandardCharsets.UTF_8.toString())

        // Route must match navgraph: bill_screen/{fare}/{role}?pickupAddress={pickupAddress}&dropAddress={dropAddress}
        val route = "bill_screen/$fare/$role?pickupAddress=$encodedPickup&dropAddress=$encodedDrop"
        _navigateToBill.emit(route)
    }

    // Helper function to convert Lat/Lng to Address String
    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLatLng(lat: Double?, lng: Double?): String? {
        if (lat == null || lng == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}