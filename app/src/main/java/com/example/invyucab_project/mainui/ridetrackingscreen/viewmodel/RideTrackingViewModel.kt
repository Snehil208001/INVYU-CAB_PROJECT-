package com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel

import android.content.Context
import android.location.Geocoder
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase,
    @ApplicationContext private val context: Context // Injected to use Geocoder
) : BaseViewModel() {

    val startRideSuccess = mutableStateOf(false)

    // State for the real route path
    private val _routePolyline = mutableStateOf<List<LatLng>>(emptyList())
    val routePolyline: State<List<LatLng>> = _routePolyline

    // State to hold the Rider's Phone Number (fetched from Firestore)
    private val _riderPhone = mutableStateOf<String?>(null)
    val riderPhone: State<String?> = _riderPhone

    // State to track Ride Status (e.g., "cancelled", "completed")
    private val _rideStatus = mutableStateOf<String?>(null)
    val rideStatus: State<String?> = _rideStatus

    // SharedFlow for navigating to BillScreen
    private val _navigateToBill = MutableSharedFlow<String>()
    val navigateToBill = _navigateToBill.asSharedFlow()

    // Local variables to store coordinates for reverse geocoding if needed
    private var currentPickupLatLng: LatLng? = null
    private var currentDropLatLng: LatLng? = null

    // Function to fetch Rider Phone from Firestore
    fun fetchRiderDetails(riderId: Int) {
        viewModelScope.launch {
            val phone = appRepository.getPhoneFromFirestore(riderId)
            if (phone != null) {
                _riderPhone.value = phone
            }
        }
    }

    // Function to monitor Ride Status (Polling) - RIDER SIDE
    fun monitorRideStatus(rideId: Int) {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = appRepository.getOngoingRideRiderSide(rideId)
                    if (response.isSuccessful && response.body() != null) {
                        val ride = response.body()?.data?.firstOrNull()
                        if (ride != null) {
                            _rideStatus.value = ride.status

                            // Check if ride is completed
                            if (ride.status == "completed") {
                                val fare = ride.totalAmount ?: ride.price ?: ride.estimatedPrice ?: "0.0"

                                // ✅ FIX: If address is missing from API, generate it from coordinates
                                val finalPickup = ride.pickupAddress
                                    ?: getAddressFromLatLng(ride.pickupLatitude?.toDoubleOrNull(), ride.pickupLongitude?.toDoubleOrNull())
                                    ?: "Unknown Pickup"

                                val finalDrop = ride.dropAddress
                                    ?: getAddressFromLatLng(ride.dropLatitude?.toDoubleOrNull(), ride.dropLongitude?.toDoubleOrNull())
                                    ?: "Unknown Drop"

                                navigateToBillScreen(
                                    fare = fare.toString(),
                                    role = "rider",
                                    pickup = finalPickup,
                                    drop = finalDrop
                                )
                                break // Stop polling
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    // Function for Driver to Complete Ride - DRIVER SIDE
    fun completeRide(rideId: Int, currentFare: String, pickupAddress: String?, dropAddress: String?) {
        viewModelScope.launch {
            try {
                // Call API to update status to "completed"
                val response = appRepository.updateRideStatus(rideId, "completed")

                if (response.isSuccessful) {
                    sendEvent(UiEvent.ShowSnackbar("Ride Completed successfully"))

                    // ✅ FIX: Ensure we have valid addresses before navigating
                    // Use passed addresses -> fallback to Reverse Geocoding -> fallback to "Unknown"
                    val finalPickup = if (!pickupAddress.isNullOrEmpty()) pickupAddress
                    else getAddressFromLatLng(currentPickupLatLng?.latitude, currentPickupLatLng?.longitude)
                        ?: "Pickup Location"

                    val finalDrop = if (!dropAddress.isNullOrEmpty()) dropAddress
                    else getAddressFromLatLng(currentDropLatLng?.latitude, currentDropLatLng?.longitude)
                        ?: "Drop Location"

                    navigateToBillScreen(
                        fare = currentFare,
                        role = "driver",
                        pickup = finalPickup,
                        drop = finalDrop
                    )
                } else {
                    sendEvent(UiEvent.ShowSnackbar(response.message() ?: "Failed to complete ride"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    // Helper to format and emit the BillScreen route
    private suspend fun navigateToBillScreen(fare: String, role: String, pickup: String, drop: String) {
        val encodedPickup = URLEncoder.encode(pickup, StandardCharsets.UTF_8.toString())
        val encodedDrop = URLEncoder.encode(drop, StandardCharsets.UTF_8.toString())

        val route = "bill_screen/$fare/$role?pickupAddress=$encodedPickup&dropAddress=$encodedDrop"
        _navigateToBill.emit(route)
    }

    // Fetch route and store coordinates
    fun fetchRoute(pickupLat: Double, pickupLng: Double, dropLat: Double, dropLng: Double) {
        // ✅ Store for later use in completeRide
        currentPickupLatLng = LatLng(pickupLat, pickupLng)
        currentDropLatLng = LatLng(dropLat, dropLng)

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

    // ✅ NEW HELPER: Reverse Geocoding to get Address String from Lat/Lng
    private suspend fun getAddressFromLatLng(lat: Double?, lng: Double?): String? {
        if (lat == null || lng == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) // Return the first address line
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