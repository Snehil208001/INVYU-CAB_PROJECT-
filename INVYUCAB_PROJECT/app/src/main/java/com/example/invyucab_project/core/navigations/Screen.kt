package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")
    object SplashScreenLoggedIn : Screen("splash_screen_logged_in")

    object OtpScreen :
        Screen("otp_screen/{phone}/{isSignUp}/{role}?name={name}&gender={gender}&dob={dob}&license={license}&vehicle={vehicle}&aadhaar={aadhaar}") {
        fun createRoute(
            phone: String,
            isSignUp: Boolean,
            role: String,
            name: String?,
            gender: String?,
            dob: String?,
            license: String? = null,
            vehicle: String? = null,
            aadhaar: String? = null
        ): String {
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedGender = gender?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedDob = dob?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedLicense = license?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedVehicle = vehicle?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedAadhaar = aadhaar?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""

            return "otp_screen/$phone/$isSignUp/$role?name=$encodedName&gender=$encodedGender&dob=$encodedDob&license=$encodedLicense&vehicle=$encodedVehicle&aadhaar=$encodedAadhaar"
        }
    }

    object UserDetailsScreen : Screen("user_details_screen/{phone}/{role}?name={name}") {
        fun createRoute(
            phone: String,
            role: String,
            name: String?
        ): String {
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "user_details_screen/$phone/$role?name=$encodedName"
        }
    }

    object RoleSelectionScreen : Screen("role_selection_screen/{phone}") {
        fun createRoute(phone: String): String {
            return "role_selection_screen/$phone"
        }
    }

    object AdminScreen : Screen("admin_screen")
    object DriverScreen : Screen("driver_screen")

    object DriverDetailsScreen :
        Screen("driver_details_screen/{phone}/{role}/{name}/{gender}/{dob}") {
        fun createRoute(
            phone: String,
            role: String,
            name: String,
            gender: String,
            dob: String
        ): String {
            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
            val encodedGender = URLEncoder.encode(gender, StandardCharsets.UTF_8.toString())
            val encodedDob = URLEncoder.encode(dob, StandardCharsets.UTF_8.toString())

            return "driver_details_screen/$phone/$role/$encodedName/$encodedGender/$encodedDob"
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
    // object LocationSearchScreen : Screen("location_search_screen") // ❌ REMOVED

    object RideSelectionScreen :
        Screen("ride_selection_screen/{dropPlaceId}/{dropDescription}?pickupPlaceId={pickupPlaceId}&pickupDescription={pickupDescription}") {
        fun createRoute(
            dropPlaceId: String,
            dropDescription: String,
            pickupPlaceId: String?,
            pickupDescription: String
        ): String {
            val encodedDropDesc = URLEncoder.encode(dropDescription, StandardCharsets.UTF_8.toString())
            val encodedPickupId = pickupPlaceId?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) }
                ?: "current_location"
            val encodedPickupDesc =
                URLEncoder.encode(pickupDescription, StandardCharsets.UTF_8.toString())
            return "ride_selection_screen/$dropPlaceId/$encodedDropDesc?pickupPlaceId=$encodedPickupId&pickupDescription=$encodedPickupDesc"
        }
    }
}