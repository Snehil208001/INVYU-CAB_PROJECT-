package com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.StartRideRequest
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase // ✅ Inject UseCase
) : BaseViewModel() {

    val startRideSuccess = mutableStateOf(false)

    // ✅ State for the real route path
    private val _routePolyline = mutableStateOf<List<LatLng>>(emptyList())
    val routePolyline: State<List<LatLng>> = _routePolyline

    // ✅ Function to fetch the real road path
    fun fetchRoute(pickupLat: Double, pickupLng: Double, dropLat: Double, dropLng: Double) {
        val origin = LatLng(pickupLat, pickupLng)
        val destination = LatLng(dropLat, dropLng)

        getDirectionsAndRouteUseCase(origin, destination).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.polyline?.let { points ->
                        _routePolyline.value = points
                    }
                }
                is Resource.Error -> {
                    sendEvent(UiEvent.ShowSnackbar(result.message ?: "Failed to load route"))
                }
                is Resource.Loading -> {
                    // Optional: Show loading state
                }
            }
        }.launchIn(viewModelScope)
    }

    fun startRide(rideId: Int, riderId: Int, driverId: Int, otp: String) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val request = StartRideRequest(
                    rideId = rideId,
                    riderId = riderId,
                    driverId = driverId,
                    userPin = otp.toIntOrNull() ?: 0,
                    startedAt = sdf.format(Date())
                )

                val response = appRepository.startRideFromDriverSide(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    startRideSuccess.value = true
                    sendEvent(UiEvent.ShowSnackbar(response.body()?.message ?: "Ride Started!"))
                } else {
                    sendEvent(UiEvent.ShowSnackbar(response.body()?.message ?: "Failed"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }
}