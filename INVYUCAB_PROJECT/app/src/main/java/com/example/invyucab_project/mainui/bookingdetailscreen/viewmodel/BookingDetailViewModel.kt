package com.example.invyucab_project.mainui.bookingdetailscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val getOngoingRideUseCase: GetOngoingRideUseCase,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase // ✅ Injected
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
                    val responseBody = response.body()!!
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