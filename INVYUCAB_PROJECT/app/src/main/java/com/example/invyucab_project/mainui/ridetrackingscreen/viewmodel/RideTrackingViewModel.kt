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
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase
) : BaseViewModel() {

    val startRideSuccess = mutableStateOf(false)

    // State for the real route path
    private val _routePolyline = mutableStateOf<List<LatLng>>(emptyList())
    val routePolyline: State<List<LatLng>> = _routePolyline

    // ✅ ADDED: State to hold the Rider's Phone Number (fetched from Firestore)
    private val _riderPhone = mutableStateOf<String?>(null)
    val riderPhone: State<String?> = _riderPhone

    // ✅ ADDED: Function to fetch Rider Phone from Firestore
    // This is vital because the API doesn't send the phone number
    fun fetchRiderDetails(riderId: Int) {
        viewModelScope.launch {
            val phone = appRepository.getPhoneFromFirestore(riderId)
            if (phone != null) {
                _riderPhone.value = phone
            }
        }
    }

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

    // ✅ UPDATED: Function to call the other party
    // Handles case where phone number was not passed via navigation
    fun initiateCall(targetPhone: String?) {
        val phoneToCall = targetPhone ?: _riderPhone.value // Use fetched phone if arg is null

        if (phoneToCall.isNullOrEmpty()) {
            sendEvent(UiEvent.ShowSnackbar("Rider number not available"))
            return
        }

        viewModelScope.launch {
            try {
                val response = appRepository.initiateCall(phoneToCall)
                if (response.isSuccessful && response.body()?.success == true) {
                    sendEvent(UiEvent.ShowSnackbar("Connecting Call..."))
                } else {
                    sendEvent(UiEvent.ShowSnackbar("Call Failed: ${response.body()?.message ?: response.message()}"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }
}