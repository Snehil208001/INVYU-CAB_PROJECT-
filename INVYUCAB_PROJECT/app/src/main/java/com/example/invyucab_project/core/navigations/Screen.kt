package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")

    // ✅ MODIFIED: Added 'name', 'gender' and 'dob'
    object OtpScreen : Screen("otp_screen/{phone}/{isSignUp}?email={email}&name={name}&gender={gender}&dob={dob}") {
        fun createRoute(phone: String, isSignUp: Boolean, email: String?, name: String?, gender: String?, dob: String?): String {
            val encodedEmail = email?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedGender = gender?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedDob = dob?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "otp_screen/$phone/$isSignUp?email=$encodedEmail&name=$encodedName&gender=$encodedGender&dob=$encodedDob"
        }
    }

    object UserDetailsScreen : Screen("user_details_screen?phone={phone}&email={email}&name={name}") {
        fun createRoute(phone: String?, email: String?, name: String?): String {
            val encodedPhone = phone?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedEmail = email?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "user_details_screen?phone=$encodedPhone&email=$encodedEmail&name=$encodedName"
        }
    }

    // ✅ ADDED: New route for Role Selection
    object RoleSelectionScreen : Screen("role_selection_screen?phone={phone}&email={email}&name={name}&gender={gender}&dob={dob}") {
        fun createRoute(phone: String?, email: String?, name: String?, gender: String?, dob: String?): String {
            val encodedPhone = phone?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedEmail = email?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedGender = gender?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedDob = dob?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "role_selection_screen?phone=$encodedPhone&email=$encodedEmail&name=$encodedName&gender=$encodedGender&dob=$encodedDob"
        }
    }

    // ✅ ADDED: New screens for Admin and Driver
    object AdminScreen : Screen("admin_screen")
    object DriverScreen : Screen("driver_screen")

    // ✅ ADDED: New route for Driver Details
    object DriverDetailsScreen : Screen("driver_details_screen?phone={phone}&email={email}&name={name}&gender={gender}&dob={dob}") {
        fun createRoute(phone: String?, email: String?, name: String?, gender: String?, dob: String?): String {
            val encodedPhone = phone?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedEmail = email?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedGender = gender?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedDob = dob?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "driver_details_screen?phone=$encodedPhone&email=$encodedEmail&name=$encodedName&gender=$encodedGender&dob=$encodedDob"
        }
    }

    // Routes for main app sections
    object HomeScreen : Screen("home_screen")
    object AllServicesScreen : Screen("all_services_screen")
    object TravelScreen : Screen("travel_screen")
    object ProfileScreen : Screen("profile_screen")
    object EditProfileScreen : Screen("edit_profile_screen")
    object MemberLevelScreen : Screen("member_level_screen")
    object PaymentMethodScreen : Screen("payment_method_screen")
    object LocationSearchScreen : Screen("location_search_screen")

    object RideSelectionScreen : Screen("ride_selection_screen/{placeId}/{description}") {
        fun createRoute(placeId: String, description: String): String {
            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            return "ride_selection_screen/$placeId/$encodedDescription"
        }
    }
}