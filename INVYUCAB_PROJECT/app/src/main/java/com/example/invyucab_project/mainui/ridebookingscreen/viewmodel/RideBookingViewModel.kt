package com.example.invyucab_project.mainui.ridebookingscreen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.models.RideBookingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideBookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // TODO: Inject UseCases to get ride details and map route
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideBookingUiState())
    val uiState = _uiState.asStateFlow()

    // ✅ FIXED: Retrieve as Int (because NavType.IntType was used), then convert to String
    private val rideId: String? = savedStateHandle.get<Int>("rideId")?.toString()

    init {
        _uiState.update { it.copy(rideId = rideId) }
        fetchRideDetails()
    }

    private fun fetchRideDetails() {
        viewModelScope.launch {
            // TODO: Use rideId to fetch ride details from your API
            // For now, we'll just use dummy data after a delay
            kotlinx.coroutines.delay(2000) // Simulate network call

            // Dummy data based on your "Schedule Ride-2.jpg"
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pickupDescription = "Invyu, Sector 2",
                    dropDescription = "Invyu, Sector 5",
                    isSearchingForDriver = true
                    // TODO: Set pickupLocation, dropLocation, and routePolyline
                )
            }
        }
    }

    fun onCancelRide() {
        // TODO: Add logic to cancel the ride via API
        _uiState.update { it.copy(isSearchingForDriver = false) }
    }
}