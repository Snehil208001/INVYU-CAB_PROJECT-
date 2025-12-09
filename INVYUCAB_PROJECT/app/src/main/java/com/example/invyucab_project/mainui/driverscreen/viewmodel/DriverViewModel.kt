package com.example.invyucab_project.mainui.driverscreen.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.StartRideRequest
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.domain.usecase.GetVehicleDetailsUseCase
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RideRequestItem(
    val rideId: Int,
    val riderId: Int = 0,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropLat: Double,
    val dropLng: Double,
    val price: Double,
    val pickupAddress: String,
    val dropAddress: String,
    val pickupDistance: String = "0 km",
    val tripDistance: String = "0 km"
)

data class RideHistoryUiModel(
    val rideId: Int,
    val pickup: String,
    val drop: String,
    val price: String,
    val status: String,
    val date: String
)

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: LocationManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getVehicleDetailsUseCase: GetVehicleDetailsUseCase,
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _showVehicleBanner = MutableStateFlow(false)
    val showVehicleBanner: StateFlow<Boolean> = _showVehicleBanner.asStateFlow()

    private val _rideRequests = MutableStateFlow<List<RideRequestItem>>(emptyList())
    val rideRequests: StateFlow<List<RideRequestItem>> = _rideRequests.asStateFlow()

    private val _totalRides = MutableStateFlow<List<RideHistoryUiModel>>(emptyList())
    val totalRides: StateFlow<List<RideHistoryUiModel>> = _totalRides.asStateFlow()

    private val _ongoingRides = MutableStateFlow<List<RideRequestItem>>(emptyList())
    val ongoingRides: StateFlow<List<RideRequestItem>> = _ongoingRides.asStateFlow()

    private val _navigateToTab = MutableSharedFlow<String>()
    val navigateToTab: SharedFlow<String> = _navigateToTab.asSharedFlow()

    private val _declinedRideIds = mutableSetOf<Int>()
    private var pollingJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _currentLocation.value = LatLng(location.latitude, location.longitude)
                if (_isActive.value) {
                    updateBackendLocation(location)
                }
            }
        }
    }

    init {
        getCurrentLocation()
        // ✅ FIXED: Check saved status on initialization
        val savedStatus = userPreferencesRepository.getDriverOnlineStatus()
        _isActive.value = savedStatus

        if (savedStatus) {
            // Restore operations if driver was online
            startLocationUpdates()
            startLookingForRides()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return "N/A"
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val distanceInMeters = results[0]
        return String.format("%.1f km", distanceInMeters / 1000)
    }

    fun checkVehicleDetails() {
        viewModelScope.launch {
            val driverId = userPreferencesRepository.getUserId()
            if (driverId == null) {
                _showVehicleBanner.value = true
                return@launch
            }
            getVehicleDetailsUseCase(driverId).onEach { result ->
                when (result) {
                    is Resource.Success -> _showVehicleBanner.value = (result.data == null)
                    is Resource.Error -> _showVehicleBanner.value = true
                    else -> {}
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                _currentLocation.value = LatLng(location.latitude, location.longitude)
                updateBackendLocation(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!isLocationEnabled()) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateBackendLocation(location: Location) {
        viewModelScope.launch {
            try {
                val driverIdStr = userPreferencesRepository.getUserId()
                val driverId = driverIdStr?.toIntOrNull()
                if (driverId != null) {
                    appRepository.updateDriverLocation(driverId, location.latitude, location.longitude, _isActive.value)
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error updating location: ${e.message}")
            }
        }
    }

    fun onActiveToggleChanged(active: Boolean) {
        _isActive.value = active
        // ✅ FIXED: Save the new status to preferences
        userPreferencesRepository.saveDriverOnlineStatus(active)

        if (active) {
            startLocationUpdates()
            startLookingForRides()
        } else {
            stopLocationUpdates()
            stopLookingForRides()
            _rideRequests.value = emptyList()
            _declinedRideIds.clear()
            _currentLocation.value?.let {
                val loc = Location("").apply { latitude = it.latitude; longitude = it.longitude }
                updateBackendLocation(loc)
            }
        }
    }

    private fun parsePrice(value: Any?): Double {
        if (value == null) return 0.0
        return try {
            when (value) {
                is Number -> value.toDouble()
                is String -> {
                    val cleanString = value.replace(Regex("[^0-9.]"), "")
                    cleanString.toDoubleOrNull() ?: 0.0
                }
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    fun fetchTotalRides() {
        viewModelScope.launch {
            try {
                val driverIdStr = userPreferencesRepository.getUserId()
                val driverId = driverIdStr?.toIntOrNull()
                if (driverId != null) {
                    val response = appRepository.getDriverTotalRides(driverId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val rides = response.body()?.data ?: emptyList()
                        _totalRides.value = rides.map { item ->
                            val pLat = item.pickupLatitude?.toString()?.toDoubleOrNull() ?: 0.0
                            val pLng = item.pickupLongitude?.toString()?.toDoubleOrNull() ?: 0.0
                            val dLat = item.dropLatitude?.toString()?.toDoubleOrNull() ?: 0.0
                            val dLng = item.dropLongitude?.toString()?.toDoubleOrNull() ?: 0.0
                            val pickup = item.pickupAddress?.takeIf { it.isNotBlank() }
                                ?: item.pickupLocation?.takeIf { it.isNotBlank() }
                                ?: getAddressFromCoordinates(pLat, pLng)
                            val drop = item.dropAddress?.takeIf { it.isNotBlank() }
                                ?: item.dropLocation?.takeIf { it.isNotBlank() }
                                ?: getAddressFromCoordinates(dLat, dLng)
                            val priceVal = parsePrice(item.totalAmount).takeIf { it > 0 }
                                ?: parsePrice(item.price).takeIf { it > 0 }
                                ?: parsePrice(item.amount).takeIf { it > 0 }
                                ?: parsePrice(item.estimatedPrice).takeIf { it > 0 }
                                ?: parsePrice(item.fare).takeIf { it > 0 }
                                ?: 0.0
                            val dateVal = item.date ?: item.createdAt ?: ""
                            RideHistoryUiModel(
                                rideId = item.rideId ?: 0,
                                pickup = pickup,
                                drop = drop,
                                price = String.format("%.2f", priceVal),
                                status = item.status ?: "completed",
                                date = dateVal
                            )
                        }.reversed()
                    }
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching total rides: ${e.message}")
            }
        }
    }

    fun fetchOngoingRides() {
        viewModelScope.launch {
            try {
                val driverIdStr = userPreferencesRepository.getUserId()
                val driverId = driverIdStr?.toIntOrNull()
                if (driverId != null) {
                    val response = appRepository.getDriverOngoingRides(driverId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val rides = response.body()?.data ?: emptyList()
                        val mappedRides = mutableListOf<RideRequestItem>()
                        val currentLoc = _currentLocation.value
                        for (item in rides) {
                            if (item.rideId != null) {
                                val pLat = item.pickupLatitude?.toDoubleOrNull() ?: 0.0
                                val pLng = item.pickupLongitude?.toDoubleOrNull() ?: 0.0
                                val dLat = item.dropLatitude?.toDoubleOrNull() ?: 0.0
                                val dLng = item.dropLongitude?.toDoubleOrNull() ?: 0.0
                                val pickup = if (!item.pickupAddress.isNullOrBlank()) item.pickupAddress
                                else if (!item.pickupLocation.isNullOrBlank()) item.pickupLocation
                                else getAddressFromCoordinates(pLat, pLng)
                                val drop = if (!item.dropAddress.isNullOrBlank()) item.dropAddress
                                else if (!item.dropLocation.isNullOrBlank()) item.dropLocation
                                else getAddressFromCoordinates(dLat, dLng)
                                val finalPrice = parsePrice(item.totalAmount ?: item.price ?: item.estimatedPrice)
                                val riderId = item.riderId ?: 0
                                val distDriverToPickup = if (currentLoc != null) {
                                    calculateDistance(currentLoc.latitude, currentLoc.longitude, pLat, pLng)
                                } else "N/A"
                                val distTrip = calculateDistance(pLat, pLng, dLat, dLng)
                                mappedRides.add(
                                    RideRequestItem(
                                        rideId = item.rideId,
                                        riderId = riderId,
                                        pickupLat = pLat,
                                        pickupLng = pLng,
                                        dropLat = dLat,
                                        dropLng = dLng,
                                        price = finalPrice,
                                        pickupAddress = pickup,
                                        dropAddress = drop,
                                        pickupDistance = distDriverToPickup,
                                        tripDistance = distTrip
                                    )
                                )
                            }
                        }
                        _ongoingRides.value = mappedRides.reversed()
                    } else {
                        _ongoingRides.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching ongoing rides: ${e.message}")
            }
        }
    }

    fun onStartRideClicked(ride: RideRequestItem) {
        viewModelScope.launch {
            val driverIdStr = userPreferencesRepository.getUserId()
            val driverId = driverIdStr?.toIntOrNull()
            if (driverId != null) {
                sendEvent(
                    UiEvent.Navigate(
                        Screen.RideTrackingScreen.createRoute(
                            rideId = ride.rideId,
                            riderId = ride.riderId,
                            driverId = driverId,
                            role = "driver",
                            pickupLat = ride.pickupLat,
                            pickupLng = ride.pickupLng,
                            dropLat = ride.dropLat,
                            dropLng = ride.dropLng,
                            otp = "1234"
                        )
                    )
                )
            } else {
                sendEvent(UiEvent.ShowSnackbar("Driver ID missing. Please relogin."))
            }
        }
    }

    // ✅ FIXED: Now calls API to update status, not just local remove
    fun onCancelOngoingRide(ride: RideRequestItem) {
        viewModelScope.launch {
            try {
                // Call backend API to cancel
                val response = appRepository.updateRideStatus(ride.rideId, "cancelled")

                if (response.isSuccessful && response.body()?.success == true) {
                    // Success: Remove from local list
                    _ongoingRides.value = _ongoingRides.value.filter { it.rideId != ride.rideId }
                    sendEvent(UiEvent.ShowSnackbar("Ride cancelled successfully."))

                    // Refresh list from server to be sure
                    fetchOngoingRides()
                } else {
                    val msg = response.body()?.message ?: response.message()
                    sendEvent(UiEvent.ShowSnackbar("Failed to cancel: $msg"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
                e.printStackTrace()
            }
        }
    }

    private fun startLookingForRides() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (_isActive.value) {
                try {
                    val loc = _currentLocation.value
                    val driverIdStr = userPreferencesRepository.getUserId()
                    val driverId = driverIdStr?.toIntOrNull()
                    if (loc != null && driverId != null) {
                        val response = appRepository.getDriverUpcomingRides(
                            driverId = driverId,
                            lat = loc.latitude,
                            lng = loc.longitude
                        )
                        if (response.isSuccessful && response.body()?.success == true) {
                            val ridesData = response.body()?.data ?: emptyList()
                            val mappedRides = ridesData.mapNotNull { ride ->
                                if (ride.rideId != null && !_declinedRideIds.contains(ride.rideId)) {
                                    val finalPrice = parsePrice(ride.estimatedPrice).takeIf { it > 0 }
                                        ?: parsePrice(ride.fare).takeIf { it > 0 }
                                        ?: parsePrice(ride.totalPrice).takeIf { it > 0 }
                                        ?: parsePrice(ride.price).takeIf { it > 0 }
                                        ?: parsePrice(ride.amount).takeIf { it > 0 }
                                        ?: parsePrice(ride.totalAmount).takeIf { it > 0 }
                                        ?: parsePrice(ride.estimatedFare).takeIf { it > 0 }
                                        ?: parsePrice(ride.cost).takeIf { it > 0 }
                                        ?: 0.0
                                    val pLat = ride.pickupLatitude?.toDoubleOrNull() ?: 0.0
                                    val pLng = ride.pickupLongitude?.toDoubleOrNull() ?: 0.0
                                    val dLat = ride.dropLatitude?.toDoubleOrNull() ?: 0.0
                                    val dLng = ride.dropLongitude?.toDoubleOrNull() ?: 0.0
                                    val finalPickupAddress = if (!ride.pickupAddress.isNullOrEmpty()) ride.pickupAddress
                                    else if (!ride.pickupLocation.isNullOrEmpty()) ride.pickupLocation
                                    else getAddressFromCoordinates(pLat, pLng)
                                    val finalDropAddress = if (!ride.dropAddress.isNullOrEmpty()) ride.dropAddress
                                    else if (!ride.dropLocation.isNullOrEmpty()) ride.dropLocation
                                    else getAddressFromCoordinates(dLat, dLng)
                                    val distDriverToPickup = calculateDistance(loc.latitude, loc.longitude, pLat, pLng)
                                    val distTrip = calculateDistance(pLat, pLng, dLat, dLng)
                                    RideRequestItem(
                                        rideId = ride.rideId,
                                        riderId = 0,
                                        pickupLat = pLat,
                                        pickupLng = pLng,
                                        dropLat = dLat,
                                        dropLng = dLng,
                                        price = finalPrice,
                                        pickupAddress = finalPickupAddress,
                                        dropAddress = finalDropAddress,
                                        pickupDistance = distDriverToPickup,
                                        tripDistance = distTrip
                                    )
                                } else null
                            }.reversed()
                            _rideRequests.value = mappedRides
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DriverViewModel", "Error fetching rides: ${e.message}")
                }
                delay(5000)
            }
        }
    }

    private fun stopLookingForRides() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        if (lat == 0.0 || lng == 0.0) return "Invalid Location"
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Lat: $lat, Lng: $lng"
                } else {
                    "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
                }
            } catch (e: Exception) {
                "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
            }
        }
    }

    fun onAcceptRide(ride: RideRequestItem) {
        viewModelScope.launch {
            _apiError.value = "Accepting ride..."
            try {
                val driverIdStr = userPreferencesRepository.getUserId()
                val driverId = driverIdStr?.toIntOrNull()

                if (driverId != null) {
                    val acceptResponse = appRepository.acceptRide(ride.rideId, driverId)
                    if (acceptResponse.isSuccessful && acceptResponse.body()?.success == true) {

                        // Stop polling for new rides as we've accepted one
                        stopLookingForRides()
                        _rideRequests.value = emptyList()

                        // Navigate to Ongoing Tab and Refresh
                        _navigateToTab.emit("Ongoing")
                        fetchOngoingRides()

                    } else {
                        val errorMsg = acceptResponse.body()?.message ?: "Failed to accept ride."
                        sendEvent(UiEvent.ShowSnackbar(errorMsg))
                    }
                } else {
                    sendEvent(UiEvent.ShowSnackbar("Driver ID not found."))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
                e.printStackTrace()
            }
        }
    }

    fun onDeclineRide(ride: RideRequestItem) {
        _declinedRideIds.add(ride.rideId)
        _rideRequests.value = _rideRequests.value.filter { it.rideId != ride.rideId }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            stopLocationUpdates()
            logoutUserUseCase()
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}