package com.example.invyucab_project.data.repository

import android.util.Log
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val customApiService: CustomApiService,
    private val googleMapsApiService: GoogleMapsApiService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firestore: FirebaseFirestore // ✅ INJECTED FIRESTORE
) {

    // The Message Bridge (SharedFlow)
    private val _fcmMessages = MutableSharedFlow<RemoteMessage>(extraBufferCapacity = 10)
    val fcmMessages: SharedFlow<RemoteMessage> = _fcmMessages.asSharedFlow()

    // Function called by Service to send message to UI
    suspend fun broadcastMessage(message: RemoteMessage) {
        _fcmMessages.emit(message)
    }

    // ✅ ADDED: Firestore Real-time Listener
    // Observes the 'ride_requests' collection for pending rides
    fun listenForRealtimeRides(): Flow<List<DriverUpcomingRideItem>> = callbackFlow {
        val listener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending") // Filter strictly for pending rides
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Listen failed.", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val rides = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Manually map fields to avoid Moshi/Reflection issues
                            DriverUpcomingRideItem(
                                rideId = (doc.get("rideId") as? Long)?.toInt() ?: doc.get("rideId").toString().toIntOrNull(),
                                riderId = (doc.get("riderId") as? Long)?.toInt(),
                                pickupAddress = doc.getString("pickupAddress"),
                                dropAddress = doc.getString("dropAddress"),
                                pickupLatitude = doc.getString("pickupLatitude"),
                                pickupLongitude = doc.getString("pickupLongitude"),
                                dropLatitude = doc.getString("dropLatitude"),
                                dropLongitude = doc.getString("dropLongitude"),
                                estimatedPrice = doc.getString("estimatedPrice") ?: doc.getString("price"),
                                // Fill other fields as null/optional if not in Firestore
                                status = doc.getString("status"),
                                distance = doc.getString("distance"),
                                date = doc.getString("date"),
                                pickupLocation = doc.getString("pickupLocation"),
                                dropLocation = doc.getString("dropLocation"),
                                fare = doc.getString("fare"),
                                totalPrice = doc.getString("totalPrice"),
                                price = doc.getString("price"),
                                amount = doc.getString("amount"),
                                totalAmount = doc.getString("totalAmount"),
                                estimatedFare = doc.getString("estimatedFare"),
                                cost = doc.getString("cost"),
                                createdAt = doc.getString("createdAt")
                            )
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error parsing doc: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(rides)
                }
            }
        awaitClose { listener.remove() }
    }

    // ✅ ADDED: Centralized Sync Function
    fun syncFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get Token from Firebase
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Repository retrieved token: $token")

                // 2. Save locally
                userPreferencesRepository.saveFcmToken(token)

                // 3. Check if we have a user logged in
                val phoneNumber = userPreferencesRepository.getPhoneNumber()

                if (!phoneNumber.isNullOrEmpty()) {
                    // 4. Send to Backend
                    Log.d("FCM", "Syncing token for user: $phoneNumber")
                    val response = updateFcmToken(phoneNumber, token)
                    if (response.isSuccessful) {
                        Log.d("FCM", "Token synced with backend successfully")
                    } else {
                        Log.e("FCM", "Failed to sync token: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.d("FCM", "User not logged in yet. Token saved locally, waiting for login.")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Exception in syncFcmToken", e)
            }
        }
    }

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

    suspend fun startRideFromDriverSide(request: StartRideRequest): Response<StartRideResponse> {
        return customApiService.startRideFromDriverSide(request)
    }

    suspend fun updateRideStatus(rideId: Int, status: String): Response<UpdateRideStatusResponse> {
        val request = UpdateRideStatusRequest(rideId, status)
        return customApiService.updateRideStatus(request)
    }

    suspend fun getRideHistory(userId: Int): Response<RiderRideHistoryResponse> {
        val request = RideHistoryRequest(userId = userId)
        return customApiService.getRideHistory(request)
    }

    suspend fun getOngoingRideRiderSide(rideId: Int): Response<RiderOngoingRideResponse> {
        val request = RiderOngoingRideRequest(rideId = rideId)
        return customApiService.getOngoingRideRiderSide(request)
    }

    fun getCurrentUserId(): String? {
        return userPreferencesRepository.getUserId()
    }

    suspend fun updateFcmToken(phoneNumber: String, token: String): Response<UpdateFcmTokenResponse> {
        val request = UpdateFcmTokenRequest(phoneNumber = phoneNumber, fcmToken = token)
        return customApiService.updateFcmToken(request)
    }

    fun saveFcmTokenLocally(token: String) {
        userPreferencesRepository.saveFcmToken(token)
    }

    fun getSavedPhoneNumber(): String? {
        return userPreferencesRepository.getPhoneNumber()
    }
}