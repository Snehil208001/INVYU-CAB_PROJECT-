package com.example.invyucab_project.core.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.invyucab_project.mainui.adminscreen.ui.AdminScreen
import com.example.invyucab_project.mainui.allservicesscreen.ui.AllServicesScreen
import com.example.invyucab_project.mainui.authscreen.ui.AuthScreen
import com.example.invyucab_project.mainui.bookingdetailscreen.ui.BookingDetailScreen
import com.example.invyucab_project.mainui.driverdetailsscreen.ui.DriverDetailsScreen
import com.example.invyucab_project.mainui.driverdocument.ui.DriverDocumentsScreen
import com.example.invyucab_project.mainui.driverprofilescreen.ui.DriverProfileScreen
import com.example.invyucab_project.mainui.driverscreen.ui.DriverScreen
import com.example.invyucab_project.mainui.homescreen.ui.HomeScreen
import com.example.invyucab_project.mainui.onboardingscreen.ui.OnboardingScreen
import com.example.invyucab_project.mainui.otpscreen.ui.OtpScreen
import com.example.invyucab_project.mainui.profilescreen.editprofilescreen.ui.EditProfileScreen
import com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.ui.MemberLevelScreen
import com.example.invyucab_project.mainui.profilescreen.paymentmethodscreen.ui.PaymentMethodScreen
import com.example.invyucab_project.mainui.profilescreen.ui.ProfileScreen
import com.example.invyucab_project.mainui.ridebookingscreen.ui.RideBookingScreen
import com.example.invyucab_project.mainui.rideinprogressscreen.ui.RideInProgressScreen // ✅ Import
import com.example.invyucab_project.mainui.rideselectionscreen.ui.RideSelectionScreen
import com.example.invyucab_project.mainui.ridetrackingscreen.ui.RideTrackingScreen
import com.example.invyucab_project.mainui.roleselectionscreen.ui.RoleSelectionScreen
import com.example.invyucab_project.mainui.splashscreen_loggedin.ui.SplashScreenLoggedIn
import com.example.invyucab_project.mainui.travelscreen.ui.TravelScreen
import com.example.invyucab_project.mainui.userdetailsscreen.ui.UserDetailsScreen
import com.example.invyucab_project.mainui.vehiclepreferences.ui.VehiclePreferencesScreen

@Composable
fun NavGraph(
    startDestination: String
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.SplashScreenLoggedIn.route) {
            SplashScreenLoggedIn(navController = navController)
        }
        composable(Screen.OnboardingScreen.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.AuthScreen.route) {
            AuthScreen(navController = navController)
        }
        composable(
            route = Screen.OtpScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("isSignUp") { type = NavType.BoolType },
                navArgument("role") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("gender") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("dob") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("license") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("aadhaar") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleNumber") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleModel") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleType") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleColor") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleCapacity") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            OtpScreen(navController = navController)
        }
        composable(
            route = Screen.UserDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            UserDetailsScreen(navController = navController)
        }
        composable(
            route = Screen.RoleSelectionScreen.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            RoleSelectionScreen(navController = navController)
        }
        composable(
            route = Screen.DriverDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) {
            DriverDetailsScreen(navController = navController)
        }
        composable(Screen.AdminScreen.route) { AdminScreen(navController = navController) }
        composable(Screen.DriverScreen.route) { DriverScreen(navController = navController) }
        composable(Screen.HomeScreen.route) { HomeScreen(navController = navController) }
        composable(Screen.AllServicesScreen.route) { AllServicesScreen(navController = navController) }
        composable(Screen.TravelScreen.route) { TravelScreen(navController = navController) }
        composable(Screen.ProfileScreen.route) { ProfileScreen(navController = navController) }
        composable(Screen.DriverDocumentsScreen.route) { DriverDocumentsScreen(navController = navController) }
        composable(Screen.VehiclePreferencesScreen.route) { VehiclePreferencesScreen(navController = navController) }
        composable(Screen.DriverProfileScreen.route) { DriverProfileScreen(navController = navController) }
        composable(Screen.EditProfileScreen.route) { EditProfileScreen(navController = navController) }
        composable(Screen.MemberLevelScreen.route) { MemberLevelScreen(navController = navController) }
        composable(Screen.PaymentMethodScreen.route) { PaymentMethodScreen(navController = navController) }

        composable(
            route = Screen.RideSelectionScreen.route,
            arguments = listOf(
                navArgument("dropPlaceId") { type = NavType.StringType; nullable = true },
                navArgument("dropDescription") { type = NavType.StringType; nullable = true },
                navArgument("pickupPlaceId") { type = NavType.StringType; nullable = true; defaultValue = "current_location" },
                navArgument("pickupDescription") { type = NavType.StringType; nullable = true; defaultValue = "Your Current Location" }
            )
        ) {
            RideSelectionScreen(navController = navController)
        }

        composable(
            route = Screen.RideBookingScreen.route,
            arguments = listOf(
                navArgument("rideId") { type = NavType.IntType },
                navArgument("pickupLat") { type = NavType.FloatType },
                navArgument("pickupLng") { type = NavType.FloatType },
                navArgument("dropLat") { type = NavType.FloatType },
                navArgument("dropLng") { type = NavType.FloatType },
                navArgument("pickupAddress") { type = NavType.StringType; nullable = true },
                navArgument("dropAddress") { type = NavType.StringType; nullable = true },
                navArgument("dropPlaceId") { type = NavType.StringType; nullable = true },
                navArgument("userPin") { type = NavType.IntType; defaultValue = 1234 }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getInt("rideId")
            RideBookingScreen(navController = navController, rideId = rideId?.toString())
        }

        composable(
            route = Screen.RideTrackingScreen.route,
            arguments = listOf(
                navArgument("rideId") { type = NavType.IntType },
                navArgument("riderId") { type = NavType.IntType },
                navArgument("driverId") { type = NavType.IntType },
                navArgument("role") { type = NavType.StringType },
                navArgument("pickupLat") { type = NavType.FloatType },
                navArgument("pickupLng") { type = NavType.FloatType },
                navArgument("dropLat") { type = NavType.FloatType },
                navArgument("dropLng") { type = NavType.FloatType },
                navArgument("otp") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getInt("rideId") ?: 0
            val riderId = backStackEntry.arguments?.getInt("riderId") ?: 0
            val driverId = backStackEntry.arguments?.getInt("driverId") ?: 0
            val role = backStackEntry.arguments?.getString("role") ?: "driver"
            val pickupLat = backStackEntry.arguments?.getFloat("pickupLat")?.toDouble() ?: 0.0
            val pickupLng = backStackEntry.arguments?.getFloat("pickupLng")?.toDouble() ?: 0.0
            val dropLat = backStackEntry.arguments?.getFloat("dropLat")?.toDouble() ?: 0.0
            val dropLng = backStackEntry.arguments?.getFloat("dropLng")?.toDouble() ?: 0.0
            val otp = backStackEntry.arguments?.getString("otp") ?: ""

            RideTrackingScreen(
                navController = navController,
                rideId = rideId,
                riderId = riderId,
                driverId = driverId,
                role = role,
                pickupLat = pickupLat,
                pickupLng = pickupLng,
                dropLat = dropLat,
                dropLng = dropLng,
                otp = otp
            )
        }

        composable(
            route = Screen.BookingDetailScreen.route,
            arguments = listOf(
                navArgument("driverName") { type = NavType.StringType },
                navArgument("vehicleModel") { type = NavType.StringType },
                navArgument("otp") { type = NavType.StringType },
                navArgument("rideId") { type = NavType.IntType },
                navArgument("riderId") { type = NavType.IntType },
                navArgument("driverId") { type = NavType.IntType },
                navArgument("role") { type = NavType.StringType },
                navArgument("pickupLat") { type = NavType.FloatType },
                navArgument("pickupLng") { type = NavType.FloatType },
                navArgument("dropLat") { type = NavType.FloatType },
                navArgument("dropLng") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            BookingDetailScreen(
                navController = navController,
                driverName = backStackEntry.arguments?.getString("driverName") ?: "",
                vehicleModel = backStackEntry.arguments?.getString("vehicleModel") ?: "",
                otp = backStackEntry.arguments?.getString("otp") ?: "",
                rideId = backStackEntry.arguments?.getInt("rideId") ?: 0,
                riderId = backStackEntry.arguments?.getInt("riderId") ?: 0,
                driverId = backStackEntry.arguments?.getInt("driverId") ?: 0,
                role = backStackEntry.arguments?.getString("role") ?: "driver",
                pickupLat = backStackEntry.arguments?.getFloat("pickupLat")?.toDouble() ?: 0.0,
                pickupLng = backStackEntry.arguments?.getFloat("pickupLng")?.toDouble() ?: 0.0,
                dropLat = backStackEntry.arguments?.getFloat("dropLat")?.toDouble() ?: 0.0,
                dropLng = backStackEntry.arguments?.getFloat("dropLng")?.toDouble() ?: 0.0
            )
        }

        // ✅ NEW: RideInProgressScreen Composable Registered
        composable(
            route = Screen.RideInProgressScreen.route,
            arguments = listOf(
                navArgument("rideId") { type = NavType.IntType },
                navArgument("dropLat") { type = NavType.FloatType },
                navArgument("dropLng") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getInt("rideId") ?: 0
            val dropLat = backStackEntry.arguments?.getFloat("dropLat")?.toDouble() ?: 0.0
            val dropLng = backStackEntry.arguments?.getFloat("dropLng")?.toDouble() ?: 0.0

            RideInProgressScreen(
                navController = navController,
                rideId = rideId,
                dropLat = dropLat,
                dropLng = dropLng
            )
        }
    }
}