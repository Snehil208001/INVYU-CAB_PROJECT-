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

    // ✅✅✅ START OF NEW CODE ✅✅✅
    @POST("riding_app/v1/get_pricing")
    suspend fun getPricing(@Body request: GetPricingRequest): GetPricingResponse
    // ✅✅✅ END OF NEW CODE ✅✅✅
}