package com.example.invyucab_project.data.api

import com.example.invyucab_project.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface CustomApiService {

    @POST("riding_app/v1/check_user")
    suspend fun checkUser(@Body request: CheckUserRequest): Response<CheckUserResponse>

    @POST("riding_app/v1/create_user")
    suspend fun createUser(@Body request: CreateUserRequest): CreateUserResponse

    @PUT("riding_app/v1/update_user_status")
    suspend fun updateUserStatus(@Body request: UpdateUserStatusRequest): UpdateUserStatusResponse

    @POST("riding_app/v1/get_pricing")
    suspend fun getPricing(@Body request: GetPricingRequest): GetPricingResponse

    @POST("riding_app/v1/add_vehicle")
    suspend fun addVehicle(@Body request: AddVehicleRequest): AddVehicleResponse

    @POST("riding_app/v1/get_driver_vehicle_detailes")
    suspend fun getVehicleDetails(@Body request: GetVehicleDetailsRequest): Response<GetVehicleDetailsResponse>

    @POST("riding_app/v1/create_rides")
    suspend fun createRide(@Body request: CreateRideRequest): Response<CreateRideResponse>

    @POST("riding_app/v1/update_driver_location")
    suspend fun updateDriverLocation(@Body request: UpdateDriverLocationRequest): Response<UpdateDriverLocationResponse>

    @POST("riding_app/v1/driver_upcomming_rides")
    suspend fun getDriverUpcomingRides(@Body request: DriverUpcomingRidesRequest): Response<DriverUpcomingRidesResponse>

    @POST("riding_app/v1/accept_rides_from_driverside")
    suspend fun acceptRide(@Body request: AcceptRideRequest): Response<AcceptRideResponse>

    @POST("riding_app/v1/driver_total_rides")
    suspend fun getDriverTotalRides(@Body request: DriverTotalRidesRequest): Response<DriverTotalRidesResponse>

    @POST("riding_app/v1/get_driver_ongoing_rides")
    suspend fun getDriverOngoingRides(@Body request: DriverOngoingRidesRequest): Response<DriverOngoingRidesResponse>

    @POST("riding_app/v1/start_ride_from_driver_side")
    suspend fun startRideFromDriverSide(@Body request: StartRideRequest): Response<StartRideResponse>

    @PUT("riding_app/v1/update_riding_status")
    suspend fun updateRideStatus(@Body request: UpdateRideStatusRequest): Response<UpdateRideStatusResponse>

    @POST("riding_app/v1/ride_history_from_riderside")
    suspend fun getRideHistory(@Body request: RideHistoryRequest): Response<RiderRideHistoryResponse>

    // ✅ ADDED: Ongoing Ride Endpoint (Polling)
    @POST("riding_app/v1/ongoing_ride_from_riderside")
    suspend fun getOngoingRideRiderSide(@Body request: RiderOngoingRideRequest): Response<RiderOngoingRideResponse>
}