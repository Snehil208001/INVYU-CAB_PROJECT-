package com.example.invyucab_project.data.repository

import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
// ✅✅✅ START OF FIX ✅✅✅
// The class is named PlacesAutocompleteResponse, not PlacesResponse
import com.example.invyucab_project.data.models.PlacesAutocompleteResponse
// ✅✅✅ END OF FIX ✅✅✅
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

    // ✅✅✅ START OF FIX ✅✅✅
    // Use the correct return type
    suspend fun getPlaceAutocomplete(query: String, sessionToken: String): PlacesAutocompleteResponse {
        // ✅✅✅ END OF FIX ✅✅✅
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
}