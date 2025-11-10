package com.example.invyucab_project.data.repository

import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.CheckUserRequest
import com.example.invyucab_project.data.models.CheckUserResponse
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.data.models.CreateUserResponse
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for all application data.
 * This class abstracts the origin of the data (API, preferences, database)
 * from the rest of the app.
 *
 * It is injected with all necessary data sources.
 */
@Singleton
class AppRepository @Inject constructor(
    // All your data sources are injected here
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
     * ✅✅✅ THE FIX IS HERE ✅✅✅
     * This API returns the CreateUserResponse object directly,
     * so the return type is NOT wrapped in Response<>.
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

    // --- You would add all other repository functions here ---
    // e.g.,
    // suspend fun getRidePricing(...) { ... }
    // suspend fun getDirections(...) { ... }
}