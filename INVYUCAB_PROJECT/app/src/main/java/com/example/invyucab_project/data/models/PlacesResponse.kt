package com.example.invyucab_project.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Data classes for Google Places Autocomplete API response

@JsonClass(generateAdapter = true)
data class PlacesAutocompleteResponse(
    @Json(name = "predictions") val predictions: List<Prediction>,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class Prediction(
    @Json(name = "description") val description: String,
    @Json(name = "place_id") val placeId: String,
    @Json(name = "structured_formatting") val structuredFormatting: StructuredFormatting
)

@JsonClass(generateAdapter = true)
data class StructuredFormatting(
    @Json(name = "main_text") val mainText: String,
    @Json(name = "secondary_text") val secondaryText: String
)