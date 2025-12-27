package com.example.invyucab_project.data.repository

import android.util.Log
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.*
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val customApiService: CustomApiService,
    private val googleMapsApiService: GoogleMapsApiService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firestore: FirebaseFirestore
) {

    // The Message Bridge (SharedFlow)
    private val _fcmMessages = MutableSharedFlow<RemoteMessage>(extraBufferCapacity = 10)
    val fcmMessages: SharedFlow<RemoteMessage> = _fcmMessages.asSharedFlow()

    // Track processed rides to prevent duplicate notifications globally
    private val _processedRideIds = mutableSetOf<Int>()

    // Event to tell ViewModel to switch tabs
    private val _rideAcceptedEvent = MutableSharedFlow<Unit>(replay = 1)
    val rideAcceptedEvent: SharedFlow<Unit> = _rideAcceptedEvent.asSharedFlow()

    // Function called by Service to send message to UI
    suspend fun broadcastMessage(message: RemoteMessage) {
        _fcmMessages.emit(message)
    }

    // Mark ride as processed so it doesn't ring again
    fun markRideProcessed(rideId: Int) {
        _processedRideIds.add(rideId)
    }

    // Check if ride is already handled
    fun isRideProcessed(rideId: Int): Boolean {
        return _processedRideIds.contains(rideId)
    }

    // Trigger navigation to Ongoing tab
    fun triggerRideAcceptedNavigation() {
        _rideAcceptedEvent.tryEmit(Unit)
    }

    // -------------------------------------------------------------------------
    // ✅ FIRESTORE USER DIRECTORY LOGIC
    // -------------------------------------------------------------------------

    // 1. Save User to Firestore (so others can find their phone number)
    suspend fun saveUserToFirestore(userId: Int, phoneNumber: String) {
        withContext(Dispatchers.IO) {
            try {
                val userMap = hashMapOf(
                    "user_id" to userId,
                    "phone_number" to phoneNumber
                )
                // Use userId as document ID for O(1) lookup speed
                firestore.collection("users")
                    .document(userId.toString())
                    .set(userMap, SetOptions.merge())
                    .await()
                Log.d("AppRepository", "✅ Synced user $userId to Firestore: $phoneNumber")
            } catch (e: Exception) {
                Log.e("AppRepository", "❌ Failed to sync user to Firestore", e)
            }
        }
    }

    // 2. Sync Current User (Called on App Launch)
    suspend fun syncCurrentUserToFirestore() {
        val userId = userPreferencesRepository.getUserId()?.toIntOrNull()
        val phone = userPreferencesRepository.getPhoneNumber()
        if (userId != null && !phone.isNullOrEmpty()) {
            saveUserToFirestore(userId, phone)
        }
    }

    // 3. Fetch Phone Number from Firestore (Used when calling)
    suspend fun getPhoneFromFirestore(userId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Try direct lookup by ID first
                val docSnap = firestore.collection("users")
                    .document(userId.toString())
                    .get()
                    .await()

                if (docSnap.exists()) {
                    val phone = docSnap.getString("phone_number")
                        ?: docSnap.getString("phoneNumber")
                    Log.d("AppRepository", "✅ Fetched phone for $userId: $phone")
                    return@withContext phone
                } else {
                    // Fallback query if document ID isn't matching
                    val querySnap = firestore.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()
                    if (!querySnap.isEmpty) {
                        return@withContext querySnap.documents[0].getString("phone_number")
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("AppRepository", "❌ Error fetching phone for user $userId", e)
                null
            }
        }
    }

    // -------------------------------------------------------------------------

    fun listenForRealtimeRides(): Flow<List<DriverUpcomingRideItem>> = callbackFlow {
        val listener = firestore.collection("ride_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Listen failed.", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val rides = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Robust Price Fetching (Checks snake_case AND camelCase)
                            val priceVal = doc.get("estimatedPrice")
                                ?: doc.get("estimated_price")
                                ?: doc.get("price")
                                ?: doc.get("totalPrice")
                                ?: doc.get("total_price")
                                ?: doc.get("totalAmount")
                                ?: doc.get("total_amount")
                                ?: doc.get("fare")
                                ?: doc.get("amount")

                            DriverUpcomingRideItem(
                                rideId = (doc.get("rideId") as? Long)?.toInt() ?: doc.get("ride_id")?.toString()?.toIntOrNull() ?: doc.get("rideId")?.toString()?.toIntOrNull(),
                                riderId = (doc.get("riderId") as? Long)?.toInt() ?: doc.get("rider_id")?.toString()?.toIntOrNull(),

                                // Locations (Check both camelCase and snake_case)
                                pickupAddress = doc.getString("pickupAddress") ?: doc.getString("pickup_address"),
                                dropAddress = doc.getString("dropAddress") ?: doc.getString("drop_address"),
                                pickupLatitude = doc.getString("pickupLatitude") ?: doc.getString("pickup_latitude"),
                                pickupLongitude = doc.getString("pickupLongitude") ?: doc.getString("pickup_longitude"),
                                dropLatitude = doc.getString("dropLatitude") ?: doc.getString("drop_latitude"),
                                dropLongitude = doc.getString("dropLongitude") ?: doc.getString("drop_longitude"),

                                // Assign the found price value (converted to String)
                                estimatedPrice = priceVal?.toString(),

                                status = doc.getString("status"),
                                distance = doc.getString("distance"),
                                date = doc.getString("date") ?: doc.getString("requested_at") ?: doc.getString("requestedAt"),
                                pickupLocation = doc.getString("pickupLocation") ?: doc.getString("pickup_location"),
                                dropLocation = doc.getString("dropLocation") ?: doc.getString("drop_location"),

                                // Other price fields just in case
                                fare = doc.get("fare")?.toString(),
                                totalPrice = doc.get("totalPrice")?.toString(),
                                price = doc.get("price")?.toString(),
                                amount = doc.get("amount")?.toString(),
                                totalAmount = doc.get("totalAmount")?.toString(),
                                estimatedFare = doc.get("estimatedFare")?.toString(),
                                cost = doc.get("cost")?.toString(),

                                createdAt = doc.getString("createdAt") ?: doc.getString("created_at")
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

    fun syncFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                userPreferencesRepository.saveFcmToken(token)
                val phoneNumber = userPreferencesRepository.getPhoneNumber()

                if (!phoneNumber.isNullOrEmpty()) {
                    val response = updateFcmToken(phoneNumber, token)
                    if (!response.isSuccessful) {
                        Log.e("FCM", "Failed to sync token: ${response.errorBody()?.string()}")
                    }
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
        val response = customApiService.checkUser(request)

        // ✅ ADDED: Auto-sync on successful login
        if (response.isSuccessful && response.body()?.existingUser != null) {
            val user = response.body()!!.existingUser!!
            saveUserToFirestore(user.userId, user.phoneNumber)
        }
        return response
    }

    suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        val response = customApiService.createUser(request)

        // ✅ ADDED: Auto-sync on successful signup
        saveUserToFirestore(response.userId, request.phoneNumber)

        return response
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