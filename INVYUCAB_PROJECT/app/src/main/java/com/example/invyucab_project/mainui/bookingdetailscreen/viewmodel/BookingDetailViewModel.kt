package com.example.invyucab_project.mainui.bookingdetailscreen.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val getOngoingRideUseCase: GetOngoingRideUseCase,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase,
    private val repository: AppRepository,
    private val application: Application
) : ViewModel() {

    private val _rideState = MutableStateFlow<Resource<RiderOngoingRideResponse>>(Resource.Loading())
    val rideState: StateFlow<Resource<RiderOngoingRideResponse>> = _rideState.asStateFlow()

    private val _routePolyline = MutableStateFlow<List<LatLng>>(emptyList())
    val routePolyline: StateFlow<List<LatLng>> = _routePolyline.asStateFlow()

    // ✅ State for Cancel Ride
    private val _cancelRideState = MutableStateFlow<Resource<Unit>?>(null)
    val cancelRideState: StateFlow<Resource<Unit>?> = _cancelRideState.asStateFlow()

    fun fetchOngoingRide(rideId: Int) {
        viewModelScope.launch {
            // FIX: Only show loading if we don't have data yet to prevent flickering during polling
            if (_rideState.value !is Resource.Success) {
                _rideState.value = Resource.Loading()
            }
            try {
                val response = getOngoingRideUseCase(rideId)
                if (response.isSuccessful && response.body() != null) {
                    var responseBody = response.body()!!
                    val updatedData = responseBody.data?.map { ride ->
                        val finalPickup = if (ride.pickupAddress.isNullOrEmpty()) getAddressFromLatLng(ride.pickupLatitude, ride.pickupLongitude) else ride.pickupAddress
                        val finalDrop = if (ride.dropAddress.isNullOrEmpty()) getAddressFromLatLng(ride.dropLatitude, ride.dropLongitude) else ride.dropAddress
                        ride.copy(pickupAddress = finalPickup, dropAddress = finalDrop)
                    }
                    responseBody = responseBody.copy(data = updatedData)
                    _rideState.value = Resource.Success(responseBody)

                    val rideItem = responseBody.data?.firstOrNull()
                    if (rideItem != null) {
                        val pickup = LatLng(rideItem.pickupLatitude?.toDoubleOrNull() ?: 0.0, rideItem.pickupLongitude?.toDoubleOrNull() ?: 0.0)
                        val drop = LatLng(rideItem.dropLatitude?.toDoubleOrNull() ?: 0.0, rideItem.dropLongitude?.toDoubleOrNull() ?: 0.0)
                        fetchRoute(pickup, drop)
                    }
                } else {
                    _rideState.value = Resource.Error(response.message() ?: "Failed to fetch ride")
                }
            } catch (e: Exception) {
                _rideState.value = Resource.Error(e.message ?: "Error loading ride")
            }
        }
    }

    // ✅ FIXED: Cancel Ride with correct status string
    fun cancelRide(rideId: Int) {
        viewModelScope.launch {
            _cancelRideState.value = Resource.Loading()
            try {
                Log.d("BookingDetailVM", "Attempting to cancel ride: $rideId")
                // Sending "cancelled" (lowercase) as required by API
                val response = repository.updateRideStatus(rideId, "cancelled")

                if (response.isSuccessful) {
                    Log.d("BookingDetailVM", "Cancellation Successful: ${response.body()}")
                    _cancelRideState.value = Resource.Success(Unit)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    Log.e("BookingDetailVM", "Cancellation Failed: $error")
                    _cancelRideState.value = Resource.Error("Failed: $error")
                }
            } catch (e: Exception) {
                Log.e("BookingDetailVM", "Exception cancelling ride", e)
                _cancelRideState.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    private suspend fun getAddressFromLatLng(latStr: String?, lngStr: String?): String {
        val lat = latStr?.toDoubleOrNull()
        val lng = lngStr?.toDoubleOrNull()
        if (lat == null || lng == null) return "Location N/A"
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(application, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) ?: "Unknown" else "Unknown"
            } catch (e: Exception) { "Unknown" }
        }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            getDirectionsAndRouteUseCase(origin, destination).collect { result ->
                if (result is Resource.Success) {
                    _routePolyline.value = result.data?.polyline ?: emptyList()
                }
            }
        }
    }
}