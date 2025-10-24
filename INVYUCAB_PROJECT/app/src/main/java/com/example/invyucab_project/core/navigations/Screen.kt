package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")
    object OtpScreen : Screen("otp_screen/{phone}?email={email}") {
        fun createRoute(phone: String, email: String?): String {
            val encodedEmail = email?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            }
            return "otp_screen/$phone" + (encodedEmail?.let { "?email=$it" } ?: "?email=")
        }
    }
    object UserDetailsScreen : Screen("user_details_screen/{phone}?email={email}") {
        fun createRoute(phone: String, email: String?): String {
            val encodedEmail = email?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            }
            return "user_details_screen/$phone" + (encodedEmail?.let { "?email=$it" } ?: "?email=")
        }
    }

    object HomeScreen : Screen("home_screen")
    object AllServicesScreen : Screen("all_services_screen")
    object TravelScreen : Screen("travel_screen")
    object ProfileScreen : Screen("profile_screen")

    object LocationSearchScreen : Screen("location_search_screen")

    // ✅ ADDED: New route for Ride Selection Screen
    // We pass the placeId and description of the selected drop location
    object RideSelectionScreen : Screen("ride_selection_screen/{placeId}/{description}") {
        fun createRoute(placeId: String, description: String): String {
            // URL-encode the description to handle special characters
            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            return "ride_selection_screen/$placeId/$encodedDescription"
        }
    }
}