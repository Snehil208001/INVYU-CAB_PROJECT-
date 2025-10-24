package com.example.invyucab_project.data.api

import com.example.invyucab_project.data.models.DirectionsResponse
import com.example.invyucab_project.data.models.PlaceDetailsResponse // ✅ ADD Import
import com.example.invyucab_project.data.models.PlacesAutocompleteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsApiService {

    @GET("maps/api/place/autocomplete/json")
    suspend fun getPlaceAutocomplete(
        @Query("input") input: String,
        @Query("sessiontoken") sessionToken: String,
        @Query("components") components: String = "country:in", // Restrict to India
        @Query("location") location: String = "25.5941,85.1376", // Bias towards Patna
        @Query("radius") radius: Int = 50000 // 50km radius bias
    ): PlacesAutocompleteResponse

    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String, // Can be lat,lng or place_id:YOUR_ID
        @Query("destination") destination: String // Can be lat,lng or place_id:YOUR_ID
    ): DirectionsResponse

    // ✅ ADD Function for Place Details
    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "geometry" // Request only geometry (lat/lng)
    ): PlaceDetailsResponse
}