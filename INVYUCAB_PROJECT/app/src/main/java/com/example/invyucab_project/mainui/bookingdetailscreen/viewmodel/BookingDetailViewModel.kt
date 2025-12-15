package com.example.invyucab_project.mainui.bookingdetailscreen.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.CreateRideRequest
import com.example.invyucab_project.data.models.RiderOngoingRideItem
import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.CreateRideUseCase
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val getOngoingRideUseCase: GetOngoingRideUseCase,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase,
    private val createRideUseCase: CreateRideUseCase, // ✅ Added for re-booking
    private val userPreferencesRepository: UserPreferencesRepository, // ✅ Added for userId
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

    // ✅ New Navigation Events
    sealed class BookingNavigationEvent {
        object NavigateHome : BookingNavigationEvent()
        data class NavigateToSearching(
            val rideId: Int,
            val userPin: Int,
            val pickupLat: Double,
            val pickupLng: Double,
            val dropLat: Double,
            val dropLng: Double,
            val pickupAddress: String,
            val dropAddress: String,
            val dropPlaceId: String
        ) : BookingNavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<BookingNavigationEvent>()
    val navigationEvent: SharedFlow<BookingNavigationEvent> = _navigationEvent.asSharedFlow()

    private var isRiderCancelling = false
    private var isRebooking = false

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
                        // ✅ Handle Status Changes
                        if (rideItem.status == "completed") {
                            _navigationEvent.emit(BookingNavigationEvent.NavigateHome)
                        } else if (rideItem.status == "cancelled") {
                            if (isRiderCancelling) {
                                // Rider initiated cancellation -> Go Home
                                _navigationEvent.emit(BookingNavigationEvent.NavigateHome)
                            } else {
                                // Driver initiated cancellation -> Auto Rebook
                                if (!isRebooking) {
                                    isRebooking = true
                                    rebookRide(rideItem)
                                }
                            }
                        }

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

    // ✅ FIXED: Cancel Ride with correct status string and flag
    fun cancelRide(rideId: Int) {
        isRiderCancelling = true // ✅ Flag that rider is cancelling
        viewModelScope.launch {
            _cancelRideState.value = Resource.Loading()
            try {
                Log.d("BookingDetailVM", "Attempting to cancel ride: $rideId")
                // Sending "cancelled" (lowercase) as required by API
                val response = repository.updateRideStatus(rideId, "cancelled")

                if (response.isSuccessful) {
                    Log.d("BookingDetailVM", "Cancellation Successful: ${response.body()}")
                    _cancelRideState.value = Resource.Success(Unit)
                    // Navigation will be handled by UI observing cancelRideState or navigationEvent
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    Log.e("BookingDetailVM", "Cancellation Failed: $error")
                    _cancelRideState.value = Resource.Error("Failed: $error")
                    isRiderCancelling = false // Reset on failure
                }
            } catch (e: Exception) {
                Log.e("BookingDetailVM", "Exception cancelling ride", e)
                _cancelRideState.value = Resource.Error(e.message ?: "Network error")
                isRiderCancelling = false // Reset on failure
            }
        }
    }

    // ✅ Logic to Rebook Ride automatically
    private fun rebookRide(oldRide: RiderOngoingRideItem) {
        viewModelScope.launch {
            try {
                val price = oldRide.estimatedPrice?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull() ?: 0.0
                val pickupLat = oldRide.pickupLatitude?.toDoubleOrNull() ?: 0.0
                val pickupLng = oldRide.pickupLongitude?.toDoubleOrNull() ?: 0.0
                val dropLat = oldRide.dropLatitude?.toDoubleOrNull() ?: 0.0
                val dropLng = oldRide.dropLongitude?.toDoubleOrNull() ?: 0.0
                val riderId = userPreferencesRepository.getUserId()?.toIntOrNull() ?: oldRide.riderId ?: 0

                val request = CreateRideRequest(
                    riderId = riderId,
                    pickupLatitude = pickupLat,
                    pickupLongitude = pickupLng,
                    dropLatitude = dropLat,
                    dropLongitude = dropLng,
                    estimatedPrice = price,
                    status = "requested"
                )

                createRideUseCase(request).collect { result ->
                    if (result is Resource.Success) {
                        val rawData = result.data?.data
                        var newRideId: Int? = null
                        var userPin: Int = 1234

                        if (rawData is Double) newRideId = rawData.toInt()
                        else if (rawData is Int) newRideId = rawData
                        else if (rawData is Map<*, *>) {
                            newRideId = (rawData["ride_id"] as? Double)?.toInt()
                            userPin = (rawData["user_pin"] as? Double)?.toInt() ?: 1234
                        }

                        if (newRideId != null) {
                            _navigationEvent.emit(BookingNavigationEvent.NavigateToSearching(
                                rideId = newRideId,
                                userPin = userPin,
                                pickupLat = pickupLat,
                                pickupLng = pickupLng,
                                dropLat = dropLat,
                                dropLng = dropLng,
                                pickupAddress = oldRide.pickupAddress ?: "",
                                dropAddress = oldRide.dropAddress ?: "",
                                dropPlaceId = "" // Place ID might be lost, rely on coords
                            ))
                        } else {
                            // Fallback if rebooking fails parsing
                            _navigationEvent.emit(BookingNavigationEvent.NavigateHome)
                        }
                    } else if (result is Resource.Error) {
                        // Fallback if rebooking API fails
                        _navigationEvent.emit(BookingNavigationEvent.NavigateHome)
                    }
                }
            } catch (e: Exception) {
                _navigationEvent.emit(BookingNavigationEvent.NavigateHome)
            } finally {
                isRebooking = false
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