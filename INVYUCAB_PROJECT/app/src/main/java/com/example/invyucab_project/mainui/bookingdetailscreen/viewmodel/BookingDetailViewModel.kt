package com.example.invyucab_project.mainui.bookingdetailscreen.viewmodel

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RiderOngoingRideResponse
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
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase, // ✅ Injected
    private val application: Application // ✅ Injected Context for Geocoder
) : ViewModel() {

    private val _rideState = MutableStateFlow<Resource<RiderOngoingRideResponse>>(Resource.Loading())
    val rideState: StateFlow<Resource<RiderOngoingRideResponse>> = _rideState.asStateFlow()

    // ✅ State for the road route polyline
    private val _routePolyline = MutableStateFlow<List<LatLng>>(emptyList())
    val routePolyline: StateFlow<List<LatLng>> = _routePolyline.asStateFlow()

    fun fetchOngoingRide(rideId: Int) {
        viewModelScope.launch {
            _rideState.value = Resource.Loading()
            try {
                val response = getOngoingRideUseCase(rideId)

                if (response.isSuccessful && response.body() != null) {
                    var responseBody = response.body()!!

                    // ✅ FIX: Reverse Geocode if address is missing
                    val updatedData = responseBody.data?.map { ride ->
                        // Check Pickup
                        val finalPickupAddress = if (ride.pickupAddress.isNullOrEmpty()) {
                            getAddressFromLatLng(ride.pickupLatitude, ride.pickupLongitude)
                        } else {
                            ride.pickupAddress
                        }

                        // Check Dropoff (optional, but good to have)
                        val finalDropAddress = if (ride.dropAddress.isNullOrEmpty()) {
                            getAddressFromLatLng(ride.dropLatitude, ride.dropLongitude)
                        } else {
                            ride.dropAddress
                        }

                        ride.copy(
                            pickupAddress = finalPickupAddress,
                            dropAddress = finalDropAddress
                        )
                    }

                    // Update response body with the list containing addresses
                    responseBody = responseBody.copy(data = updatedData)

                    _rideState.value = Resource.Success(responseBody)

                    // ✅ Trigger route fetching if we have valid coordinates
                    val rideItem = responseBody.data?.firstOrNull()
                    if (rideItem != null) {
                        val pickupLat = rideItem.pickupLatitude?.toDoubleOrNull()
                        val pickupLng = rideItem.pickupLongitude?.toDoubleOrNull()
                        val dropLat = rideItem.dropLatitude?.toDoubleOrNull()
                        val dropLng = rideItem.dropLongitude?.toDoubleOrNull()

                        if (pickupLat != null && pickupLng != null && dropLat != null && dropLng != null) {
                            fetchRoute(
                                LatLng(pickupLat, pickupLng),
                                LatLng(dropLat, dropLng)
                            )
                        }
                    }
                } else {
                    _rideState.value = Resource.Error(response.message() ?: "Failed to fetch ride details")
                }
            } catch (e: Exception) {
                _rideState.value = Resource.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    // ✅ Helper function to convert Lat/Lng to Address String
    private suspend fun getAddressFromLatLng(latStr: String?, lngStr: String?): String {
        val lat = latStr?.toDoubleOrNull()
        val lng = lngStr?.toDoubleOrNull()

        if (lat == null || lng == null) return "Location not available"

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(application, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Unknown Address"
                } else {
                    "Unknown Address"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown Address"
            }
        }
    }

    // ✅ Fetch the polyline points between pickup and dropoff
    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            getDirectionsAndRouteUseCase(origin, destination).collect { result ->
                if (result is Resource.Success) {
                    _routePolyline.value = result.data?.polyline ?: emptyList()
                }
                // You can handle errors here if needed, e.g., logging
            }
        }
    }
}