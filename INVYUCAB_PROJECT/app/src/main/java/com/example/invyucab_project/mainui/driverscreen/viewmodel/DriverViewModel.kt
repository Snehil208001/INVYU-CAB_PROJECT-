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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class RideRequestItem(
    val rideId: Int,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropLat: Double,
    val dropLng: Double,
    val price: Double,
    val pickupAddress: String,
    val dropAddress: String
)

// ✅ UI Model for History
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

    // ✅ NEW: Total Rides State
    private val _totalRides = MutableStateFlow<List<RideHistoryUiModel>>(emptyList())
    val totalRides: StateFlow<List<RideHistoryUiModel>> = _totalRides.asStateFlow()

    private val _declinedRideIds = mutableSetOf<Int>()
    private var pollingJob: Job? = null

    init {
        getCurrentLocation()
    }

    // --- Vehicle Logic ---
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

    // --- Location Logic ---
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
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            _apiError.value = "Failed to get location. Please enable GPS."
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let { location ->
                    _currentLocation.value = LatLng(location.latitude, location.longitude)
                    updateBackendLocation(location)
                } ?: run {
                    _apiError.value = "Could not fetch current location."
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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

    // --- Status Toggle ---
    fun onActiveToggleChanged(active: Boolean) {
        _isActive.value = active
        if (active) {
            startLookingForRides()
            _currentLocation.value?.let {
                val loc = Location("").apply { latitude = it.latitude; longitude = it.longitude }
                updateBackendLocation(loc)
            }
        } else {
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

    // --- ✅ NEW: Fetch Total Rides ---
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
                            val pickup = item.pickupAddress ?: item.pickupLocation ?: "Unknown Pickup"
                            val drop = item.dropAddress ?: item.dropLocation ?: "Unknown Drop"
                            val priceVal = parsePrice(item.totalAmount ?: item.price)
                            val dateVal = item.date ?: item.createdAt ?: ""

                            RideHistoryUiModel(
                                rideId = item.rideId ?: 0,
                                pickup = pickup,
                                drop = drop,
                                price = String.format("%.2f", priceVal),
                                status = item.status ?: "completed",
                                date = dateVal
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching total rides: ${e.message}")
            }
        }
    }

    // --- Polling Logic ---
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

                                    RideRequestItem(
                                        rideId = ride.rideId,
                                        pickupLat = pLat,
                                        pickupLng = pLng,
                                        dropLat = dLat,
                                        dropLng = dLng,
                                        price = finalPrice,
                                        pickupAddress = finalPickupAddress,
                                        dropAddress = finalDropAddress
                                    )
                                } else null
                            }
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
                    val response = appRepository.acceptRide(ride.rideId, driverId)

                    if (response.isSuccessful && response.body()?.success == true) {
                        sendEvent(UiEvent.ShowSnackbar(response.body()?.message ?: "Ride Accepted!"))
                        stopLookingForRides()
                        _rideRequests.value = emptyList()
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to accept ride."
                        sendEvent(UiEvent.ShowSnackbar(errorMsg))
                    }
                } else {
                    sendEvent(UiEvent.ShowSnackbar("Driver ID not found."))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    fun onDeclineRide(ride: RideRequestItem) {
        _declinedRideIds.add(ride.rideId)
        _rideRequests.value = _rideRequests.value.filter { it.rideId != ride.rideId }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUserUseCase()
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}