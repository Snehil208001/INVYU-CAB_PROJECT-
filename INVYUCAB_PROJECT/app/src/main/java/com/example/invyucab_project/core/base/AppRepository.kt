package com.example.invyucab_project.data.repository

import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for all application data.
 * This class abstracts the origin of the data (API, preferences, database)
 * from the rest of the app.
 */
@Singleton
class AppRepository @Inject constructor(
    private val customApiService: CustomApiService,
    private val googleMapsApiService: GoogleMapsApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    // --- AUTH / USER FUNCTIONS ---

    /**
     * Checks if a user exists in our custom backend.
     * This API returns a Response<> wrapper.
     */
    suspend fun checkUser(phoneNumber: String): Response<CheckUserResponse> {
        val request = CheckUserRequest(phoneNumber = phoneNumber)
        return customApiService.checkUser(request)
    }

    /**
     * Creates a new user in our custom backend.
     * This API returns the CreateUserResponse object directly.
     */
    suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        return customApiService.createUser(request)
    }

    /**
     * Saves the user's login status ("active" or "inactive") to local preferences.
     */
    suspend fun saveUserStatus(status: String) {
        userPreferencesRepository.saveUserStatus(status)
    }

    /**
     * Clears the user's login status from local preferences.
     */
    suspend fun clearUserStatus() {
        userPreferencesRepository.clearUserStatus()
    }

    // --- MAPS / RIDE FUNCTIONS ---

    /**
     * Gets ride pricing from the custom backend.
     */
    suspend fun getRidePricing(request: GetPricingRequest): GetPricingResponse {
        return customApiService.getPricing(request)
    }

    /**
     * Gets route directions from Google Maps API.
     */
    suspend fun getDirections(origin: String, destination: String): DirectionsResponse {
        return googleMapsApiService.getDirections(origin, destination)
    }

    /**
     * Gets place details from Google Maps API.
     */
    suspend fun getPlaceDetails(placeId: String): PlaceDetailsResponse {
        return googleMapsApiService.getPlaceDetails(placeId)
    }
}