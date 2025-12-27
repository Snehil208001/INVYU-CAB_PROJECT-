package com.example.invyucab_project.core.navigations

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    // ... (Keep other screens as they are) ...

    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")
    object SplashScreenLoggedIn : Screen("splash_screen_logged_in")

    // ... (Keep OtpScreen, UserDetailsScreen, etc. as they are) ...

    // ✅ 1. UPDATE RideTrackingScreen Route
    object RideTrackingScreen : Screen("ride_tracking_screen/{rideId}/{riderId}/{driverId}/{role}?pickupLat={pickupLat}&pickupLng={pickupLng}&dropLat={dropLat}&dropLng={dropLng}&otp={otp}&driverPhone={driverPhone}&riderPhone={riderPhone}") {
        fun createRoute(
            rideId: Int,
            riderId: Int,
            driverId: Int,
            role: String,
            pickupLat: Double,
            pickupLng: Double,
            dropLat: Double,
            dropLng: Double,
            otp: String = "1234",
            driverPhone: String? = null,
            riderPhone: String? = null
        ): String {
            val dPhone = driverPhone ?: ""
            val rPhone = riderPhone ?: ""
            return "ride_tracking_screen/$rideId/$riderId/$driverId/$role?pickupLat=$pickupLat&pickupLng=$pickupLng&dropLat=$dropLat&dropLng=$dropLng&otp=$otp&driverPhone=$dPhone&riderPhone=$rPhone"
        }
    }

    // ✅ 2. UPDATE RideInProgressScreen Route (Added OTP and TargetPhone)
    object RideInProgressScreen : Screen("ride_in_progress_screen/{rideId}/{dropLat}/{dropLng}?otp={otp}&targetPhone={targetPhone}") {
        fun createRoute(
            rideId: Int,
            dropLat: Double,
            dropLng: Double,
            otp: String,
            targetPhone: String? = null
        ): String {
            val tPhone = targetPhone ?: ""
            return "ride_in_progress_screen/$rideId/${dropLat.toFloat()}/${dropLng.toFloat()}?otp=$otp&targetPhone=$tPhone"
        }
    }

    // ... (Keep rest of file unchanged) ...
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

    object VehiclePreferencesScreen : Screen("vehicle_preferences_screen")

    object EditProfileScreen : Screen("edit_profile_screen")
    object MemberLevelScreen : Screen("member_level_screen")
    object PaymentMethodScreen : Screen("payment_method_screen")

    object RideHistoryScreen : Screen("ride_history_screen")

    // ✅ FIXED: Changed to Query Parameters (?) to support empty strings
    object RideSelectionScreen :
        Screen("ride_selection_screen?dropPlaceId={dropPlaceId}&dropDescription={dropDescription}&pickupPlaceId={pickupPlaceId}&pickupDescription={pickupDescription}&pickupLat={pickupLat}&pickupLng={pickupLng}&dropLat={dropLat}&dropLng={dropLng}") {
        fun createRoute(
            dropPlaceId: String,
            dropDescription: String,
            pickupPlaceId: String?,
            pickupDescription: String,
            pickupLat: Double? = null,
            pickupLng: Double? = null,
            dropLat: Double? = null,
            dropLng: Double? = null
        ): String {
            val encodedDropId = URLEncoder.encode(dropPlaceId, StandardCharsets.UTF_8.toString())
            val encodedDropDesc = URLEncoder.encode(dropDescription, StandardCharsets.UTF_8.toString())
            // Use empty string if null, instead of "current_location" if that was causing issues, or keep logic
            val encodedPickupId = pickupPlaceId?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            val encodedPickupDesc = URLEncoder.encode(pickupDescription, StandardCharsets.UTF_8.toString())

            val pLat = pickupLat ?: 0.0
            val pLng = pickupLng ?: 0.0
            val dLat = dropLat ?: 0.0
            val dLng = dropLng ?: 0.0

            return "ride_selection_screen?dropPlaceId=$encodedDropId&dropDescription=$encodedDropDesc&pickupPlaceId=$encodedPickupId&pickupDescription=$encodedPickupDesc&pickupLat=$pLat&pickupLng=$pLng&dropLat=$dLat&dropLng=$dLng"
        }
    }

    data object RideBookingScreen : Screen("ride_booking_screen/{rideId}/{pickupLat}/{pickupLng}/{dropLat}/{dropLng}?pickupAddress={pickupAddress}&dropAddress={dropAddress}&dropPlaceId={dropPlaceId}&userPin={userPin}") {
        fun createRoute(
            rideId: Int,
            pickupLat: Double,
            pickupLng: Double,
            dropLat: Double,
            dropLng: Double,
            pickupAddress: String,
            dropAddress: String,
            dropPlaceId: String,
            userPin: Int
        ): String {
            val encodedPickup = URLEncoder.encode(pickupAddress, StandardCharsets.UTF_8.toString())
            val encodedDrop = URLEncoder.encode(dropAddress, StandardCharsets.UTF_8.toString())
            val encodedDropPlaceId = URLEncoder.encode(dropPlaceId, StandardCharsets.UTF_8.toString())

            return "ride_booking_screen/$rideId/$pickupLat/$pickupLng/$dropLat/$dropLng?pickupAddress=$encodedPickup&dropAddress=$encodedDrop&dropPlaceId=$encodedDropPlaceId&userPin=$userPin"
        }
    }

    object BookingDetailScreen : Screen("booking_detail_screen/{driverName}/{vehicleModel}/{otp}/{rideId}/{riderId}/{driverId}/{role}?pickupLat={pickupLat}&pickupLng={pickupLng}&dropLat={dropLat}&dropLng={dropLng}") {
        fun createRoute(
            driverName: String,
            vehicleModel: String,
            otp: String,
            rideId: Int,
            riderId: Int,
            driverId: Int,
            role: String,
            pickupLat: Double,
            pickupLng: Double,
            dropLat: Double,
            dropLng: Double
        ): String {
            val encodedName = URLEncoder.encode(driverName, StandardCharsets.UTF_8.toString())
            val encodedModel = URLEncoder.encode(vehicleModel, StandardCharsets.UTF_8.toString())
            val encodedOtp = URLEncoder.encode(otp, StandardCharsets.UTF_8.toString())

            return "booking_detail_screen/$encodedName/$encodedModel/$encodedOtp/$rideId/$riderId/$driverId/$role?pickupLat=$pickupLat&pickupLng=$pickupLng&dropLat=$dropLat&dropLng=$dropLng"
        }
    }

    // ✅ NEW: About Us Screen
    object AboutUsScreen : Screen("about_us_screen")
}