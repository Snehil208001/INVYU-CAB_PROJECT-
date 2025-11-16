package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")
    object SplashScreenLoggedIn : Screen("splash_screen_logged_in")

    object OtpScreen :
        Screen("otp_screen/{phone}/{isSignUp}/{role}?name={name}&gender={gender}&dob={dob}&license={license}&aadhaar={aadhaar}&vehicleNumber={vehicleNumber}&vehicleModel={vehicleModel}&vehicleType={vehicleType}&vehicleColor={vehicleColor}&vehicleCapacity={vehicleCapacity}") {
        fun createRoute(
            phone: String,
            isSignUp: Boolean,
            role: String,
            name: String?,
            gender: String?,
            dob: String?,
            license: String? = null,
            aadhaar: String? = null,
            vehicleNumber: String? = null,
            vehicleModel: String? = null,
            vehicleType: String? = null,
            vehicleColor: String? = null,
            vehicleCapacity: String? = null
        ): String {
            val encodedName = name?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedGender = gender?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedDob = dob?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedLicense = license?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedAadhaar = aadhaar?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""

            val encodedVehicleNumber = vehicleNumber?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedVehicleModel = vehicleModel?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedVehicleType = vehicleType?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedVehicleColor = vehicleColor?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedVehicleCapacity = vehicleCapacity?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""

            return "otp_screen/$phone/$isSignUp/$role?name=$encodedName&gender=$encodedGender&dob=$encodedDob&license=$encodedLicense&aadhaar=$encodedAadhaar&vehicleNumber=$encodedVehicleNumber&vehicleModel=$encodedVehicleModel&vehicleType=$encodedVehicleType&vehicleColor=$encodedVehicleColor&vehicleCapacity=$encodedVehicleCapacity"
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
        Screen("driver_details_screen/{phone}/{role}") {
        fun createRoute(
            phone: String,
            role: String
        ): String {
            return "driver_details_screen/$phone/$role"
        }
    }

    object HomeScreen : Screen("home_screen")
    object AllServicesScreen : Screen("all_services_screen")
    object TravelScreen : Screen("travel_screen")

    object ProfileScreen : Screen("profile_screen")
    object DriverProfileScreen : Screen("driver_profile_screen")
    object DriverDocumentsScreen : Screen("driver_documents_screen")

    // ✅ --- NEW SCREEN ADDED ---
    object VehiclePreferencesScreen : Screen("vehicle_preferences_screen")
    // ✅ --------------------------

    object EditProfileScreen : Screen("edit_profile_screen")
    object MemberLevelScreen : Screen("member_level_screen")
    object PaymentMethodScreen : Screen("payment_method_screen")

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