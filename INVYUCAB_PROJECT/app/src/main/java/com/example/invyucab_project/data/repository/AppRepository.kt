package com.example.invyucab_project.data.repository

import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val customApiService: CustomApiService,
    private val googleMapsApiService: GoogleMapsApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun saveOnboardingCompleted() {
        userPreferencesRepository.saveOnboardingCompleted()
    }

    suspend fun checkUser(phoneNumber: String): Response<CheckUserResponse> {
        val request = CheckUserRequest(phoneNumber = phoneNumber)
        return customApiService.checkUser(request)
    }

    suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        return customApiService.createUser(request)
    }

    suspend fun addVehicle(request: AddVehicleRequest): AddVehicleResponse {
        return customApiService.addVehicle(request)
    }

    suspend fun getVehicleDetails(driverId: String): Response<GetVehicleDetailsResponse> {
        val request = GetVehicleDetailsRequest(driverId = driverId)
        return customApiService.getVehicleDetails(request)
    }

    suspend fun updateUserStatus(request: UpdateUserStatusRequest): UpdateUserStatusResponse {
        return customApiService.updateUserStatus(request)
    }

    suspend fun saveUserStatus(status: String) {
        userPreferencesRepository.saveUserStatus(status)
    }

    suspend fun clearUserStatus() {
        userPreferencesRepository.clearUserStatus()
    }

    suspend fun getPlaceAutocomplete(query: String, sessionToken: String): PlacesAutocompleteResponse {
        return googleMapsApiService.getPlaceAutocomplete(query, sessionToken)
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetailsResponse {
        return googleMapsApiService.getPlaceDetails(placeId)
    }

    suspend fun getDirections(origin: String, destination: String): DirectionsResponse {
        return googleMapsApiService.getDirections(origin, destination)
    }

    suspend fun getRidePricing(request: GetPricingRequest): GetPricingResponse {
        return customApiService.getPricing(request)
    }

    suspend fun createRide(request: CreateRideRequest): Response<CreateRideResponse> {
        return customApiService.createRide(request)
    }

    suspend fun updateDriverLocation(driverId: Int, lat: Double, lng: Double, isActive: Boolean): Response<UpdateDriverLocationResponse> {
        val request = UpdateDriverLocationRequest(driverId, lat, lng, isActive)
        return customApiService.updateDriverLocation(request)
    }

    suspend fun getDriverUpcomingRides(driverId: Int, lat: Double, lng: Double): Response<DriverUpcomingRidesResponse> {
        val request = DriverUpcomingRidesRequest(
            driverId = driverId,
            driverLatitude = lat.toString(),
            driverLongitude = lng.toString()
        )
        return customApiService.getDriverUpcomingRides(request)
    }

    suspend fun acceptRide(rideId: Int, driverId: Int): Response<AcceptRideResponse> {
        val request = AcceptRideRequest(rideId, driverId)
        return customApiService.acceptRide(request)
    }

    suspend fun getDriverTotalRides(driverId: Int): Response<DriverTotalRidesResponse> {
        val request = DriverTotalRidesRequest(driverId = driverId)
        return customApiService.getDriverTotalRides(request)
    }

    suspend fun getDriverOngoingRides(driverId: Int): Response<DriverOngoingRidesResponse> {
        val request = DriverOngoingRidesRequest(driverId = driverId)
        return customApiService.getDriverOngoingRides(request)
    }

    // --- ✅ ADDED: Start Ride Function ---
    suspend fun startRideFromDriverSide(request: StartRideRequest): Response<StartRideResponse> {
        return customApiService.startRideFromDriverSide(request)
    }

    // --- ✅ ADDED: Update Ride Status Function ---
    suspend fun updateRideStatus(rideId: Int, status: String): Response<UpdateRideStatusResponse> {
        val request = UpdateRideStatusRequest(rideId, status)
        return customApiService.updateRideStatus(request)
    }
}