package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")

    // Route for OTP Screen
    object OtpScreen : Screen("otp_screen/{phone}/{isSignUp}?email={email}") {
        fun createRoute(phone: String, isSignUp: Boolean, email: String?): String {
            val encodedEmail = email?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            } ?: "" // Use empty string if email is null
            return "otp_screen/$phone/$isSignUp?email=$encodedEmail"
        }
    }

    // Route for User Details Screen
    // ✅ MODIFIED: Added 'name' as an optional query parameter
    object UserDetailsScreen : Screen("user_details_screen/{phone}?email={email}&name={name}") {
        fun createRoute(phone: String, email: String?, name: String? = null): String {
            val encodedEmail = email?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            // ✅ MODIFIED: Encode and add the name
            val encodedName = name?.let {
                URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            } ?: ""

            // ✅ MODIFIED: Return route with both email and name
            return "user_details_screen/$phone?email=$encodedEmail&name=$encodedName"
        }
    }

    // Routes for main app sections
    object HomeScreen : Screen("home_screen")
    object AllServicesScreen : Screen("all_services_screen")
    object TravelScreen : Screen("travel_screen")
    object ProfileScreen : Screen("profile_screen")
    object EditProfileScreen : Screen("edit_profile_screen")
    object MemberLevelScreen : Screen("member_level_screen")

    // New route for Payment Methods
    object PaymentMethodScreen : Screen("payment_method_screen")

    // Route for Location Search
    object LocationSearchScreen : Screen("location_search_screen")

    // Route for Ride Selection
    object RideSelectionScreen : Screen("ride_selection_screen/{placeId}/{description}") {
        fun createRoute(placeId: String, description: String): String {
            // URL-encode the description to handle special characters
            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            return "ride_selection_screen/$placeId/$encodedDescription"
        }
    }
}