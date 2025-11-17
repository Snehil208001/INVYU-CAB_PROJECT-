package com.example.invyucab_project.data.repository

import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
import com.example.invyucab_project.data.models.PlacesAutocompleteResponse
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

    // --- ONBOARDING ---
    suspend fun saveOnboardingCompleted() {
        userPreferencesRepository.saveOnboardingCompleted()
    }

    // --- AUTH / USER FUNCTIONS ---
    suspend fun checkUser(phoneNumber: String): Response<CheckUserResponse> {
        val request = CheckUserRequest(phoneNumber = phoneNumber)
        return customApiService.checkUser(request)
    }

    suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        return customApiService.createUser(request)
    }

    /**
     * Adds a vehicle for a driver.
     */
    suspend fun addVehicle(request: AddVehicleRequest): AddVehicleResponse {
        return customApiService.addVehicle(request)
    }

    /**
     * Gets a driver's registered vehicle details.
     */
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

    // --- MAPS / RIDE FUNCTIONS ---

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

    // --- START OF ADDED CODE ---
    suspend fun createRide(request: CreateRideRequest): Response<CreateRideResponse> {
        return customApiService.createRide(request)
    }
    // --- END OF ADDED CODE ---
}