package com.example.invyucab_project.mainui.ridebookingscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RideBookingUiState
import com.example.invyucab_project.data.models.RiderOngoingRideItem
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class RideBookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase,
    private val getOngoingRideUseCase: GetOngoingRideUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideBookingUiState())
    val uiState = _uiState.asStateFlow()

    // ✅ Navigation Event Flow for Driver Acceptance
    private val _navigationEvent = MutableSharedFlow<RiderOngoingRideItem>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // ✅ Navigation Event Flow for Cancellation
    private val _navigateToSelection = MutableSharedFlow<Unit>()
    val navigateToSelection = _navigateToSelection.asSharedFlow()

    // ✅ FIXED: Safely retrieve rideId as Any to handle both Int and String types
    private val rideId: String? = savedStateHandle.get<Any>("rideId")?.toString()

    private val userPin: String? = savedStateHandle.get<Int>("userPin")?.toString()

    private val pickupLat: Double? = savedStateHandle.get<Float>("pickupLat")?.toDouble()
    private val pickupLng: Double? = savedStateHandle.get<Float>("pickupLng")?.toDouble()
    private val dropLat: Double? = savedStateHandle.get<Float>("dropLat")?.toDouble()
    private val dropLng: Double? = savedStateHandle.get<Float>("dropLng")?.toDouble()

    private val rawPickupAddress: String? = savedStateHandle.get<String>("pickupAddress")
    private val rawDropAddress: String? = savedStateHandle.get<String>("dropAddress")

    // Made public so UI can use it for navigation arguments
    val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")

    private var isPolling = true

    init {
        initializeRideDetails()
        startPollingForDriver() // ✅ Start Polling
    }

    private fun initializeRideDetails() {
        val pickupLocation = if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null
        val dropLocation = if (dropLat != null && dropLng != null) LatLng(dropLat, dropLng) else null

        val pickupAddress = decodeString(rawPickupAddress)
        val dropAddress = decodeString(rawDropAddress)

        _uiState.update {
            it.copy(
                rideId = rideId,
                userPin = userPin,
                pickupLocation = pickupLocation,
                dropLocation = dropLocation,
                pickupDescription = pickupAddress ?: "Pickup Location",
                dropDescription = dropAddress ?: "Drop Location",
                isLoading = true
            )
        }

        if (pickupLocation != null && dropPlaceId != null) {
            fetchRoute(pickupLocation, dropPlaceId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ✅ Polling Logic
    private fun startPollingForDriver() {
        // We need a valid rideId to poll
        if (rideId.isNullOrBlank()) {
            Log.e("RideBookingVM", "Ride ID not found, cannot poll.")
            return
        }
        val rideIdInt = rideId.toIntOrNull() ?: return

        viewModelScope.launch {
            while (isPolling) {
                try {
                    // Poll with rideId instead of userId
                    val response = getOngoingRideUseCase(rideIdInt)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val rides = response.body()?.data
                        // Get the ride info from the list
                        val ongoingRide = rides?.find { it.rideId == rideIdInt } ?: rides?.firstOrNull()

                        if (ongoingRide != null) {
                            Log.d("RideBookingVM", "Ride Status: ${ongoingRide.status}")

                            // ✅ CHANGED: Allow navigation if status is 'accepted' OR 'cancelled'
                            // This ensures you see the UI update even for the cancelled ride #1
                            if (ongoingRide.status == "accepted" || ongoingRide.status == "cancelled") {
                                isPolling = false
                                _navigationEvent.emit(ongoingRide)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RideBookingVM", "Polling Error: ${e.message}")
                }
                delay(5000) // Wait 5 seconds before next poll
            }
        }
    }

    private fun fetchRoute(pickup: LatLng, dropId: String) {
        viewModelScope.launch {
            getDirectionsAndRouteUseCase(pickup, dropId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                routePolyline = result.data?.polyline ?: emptyList(),
                                isSearchingForDriver = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCancelRide() {
        isPolling = false
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rideIdInt = rideId?.toIntOrNull()
                if (rideIdInt != null) {
                    // Call the API to update status to "cancelled"
                    val response = appRepository.updateRideStatus(rideIdInt, "cancelled")

                    if (response.isSuccessful) {
                        // Emit navigation event on success
                        _navigateToSelection.emit(Unit)
                    } else {
                        Log.e("RideBookingVM", "Failed to cancel ride: ${response.message()}")
                        // Even if it fails on server (e.g. network), we might still want to let user go back
                        // For now, let's navigate back to ensure user isn't stuck
                        _navigateToSelection.emit(Unit)
                    }
                }
            } catch (e: Exception) {
                Log.e("RideBookingVM", "Exception cancelling ride: ${e.message}")
                // Navigate back on exception as fallback
                _navigateToSelection.emit(Unit)
            } finally {
                _uiState.update { it.copy(isLoading = false, isSearchingForDriver = false) }
            }
        }
    }

    private fun decodeString(encoded: String?): String? {
        if (encoded == null) return null
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            encoded
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false // Ensure polling stops
    }
}