package com.example.invyucab_project.core.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// ... (All other imports)
import com.example.invyucab_project.mainui.adminscreen.ui.AdminScreen
import com.example.invyucab_project.mainui.allservicesscreen.ui.AllServicesScreen
import com.example.invyucab_project.mainui.authscreen.ui.AuthScreen
import com.example.invyucab_project.mainui.driverdetailsscreen.ui.DriverDetailsScreen
import com.example.invyucab_project.mainui.driverscreen.ui.DriverScreen
import com.example.invyucab_project.mainui.homescreen.ui.HomeScreen
import com.example.invyucab_project.mainui.onboardingscreen.ui.OnboardingScreen
import com.example.invyucab_project.mainui.otpscreen.ui.OtpScreen
import com.example.invyucab_project.mainui.profilescreen.editprofilescreen.ui.EditProfileScreen
import com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.ui.MemberLevelScreen
import com.example.invyucab_project.mainui.profilescreen.paymentmethodscreen.ui.PaymentMethodScreen
import com.example.invyucab_project.mainui.profilescreen.ui.ProfileScreen
import com.example.invyucab_project.mainui.rideselectionscreen.ui.RideSelectionScreen
import com.example.invyucab_project.mainui.roleselectionscreen.ui.RoleSelectionScreen
import com.example.invyucab_project.mainui.splashscreen_loggedin.ui.SplashScreenLoggedIn
import com.example.invyucab_project.mainui.travelscreen.ui.TravelScreen
import com.example.invyucab_project.mainui.userdetailsscreen.ui.UserDetailsScreen

@Composable
fun NavGraph(
    startDestination: String
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... (SplashScreen, OnboardingScreen, AuthScreen) ...
        composable(Screen.SplashScreenLoggedIn.route) {
            SplashScreenLoggedIn(navController = navController)
        }

        composable(Screen.OnboardingScreen.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.AuthScreen.route) {
            AuthScreen(navController = navController)
        }

        // ✅✅✅ START OF MODIFICATION ✅✅✅
        composable(
            route = Screen.OtpScreen.route,
            // Updated arguments to match the new route
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("isSignUp") { type = NavType.BoolType },
                navArgument("role") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("gender") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("dob") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("license") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("aadhaar") { type = NavType.StringType; nullable = true; defaultValue = null },
                // Add all new vehicle arguments
                navArgument("vehicleNumber") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleModel") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleType") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleColor") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("vehicleCapacity") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            OtpScreen(navController = navController)
        }
        // ✅✅✅ END OF MODIFICATION ✅✅✅

        // ... (UserDetailsScreen, RoleSelectionScreen) ...
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
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType }
            )
        ) {
            RoleSelectionScreen(navController = navController)
        }

        // ✅✅✅ START OF MODIFICATION ✅✅✅
        // This route is now correct, it no longer takes personal details
        composable(
            route = Screen.DriverDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) {
            DriverDetailsScreen(navController = navController)
        }
        // ✅✅✅ END OF MODIFICATION ✅✅✅

        composable(Screen.AdminScreen.route) {
            AdminScreen(navController = navController)
        }

        composable(Screen.DriverScreen.route) {
            DriverScreen(navController = navController)
        }

        // ... (other composables: HomeScreen, AllServicesScreen, etc.) ...
        composable(Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AllServicesScreen.route) {
            AllServicesScreen(navController = navController)
        }
        composable(Screen.TravelScreen.route) {
            TravelScreen(navController = navController)
        }
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.EditProfileScreen.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Screen.MemberLevelScreen.route) {
            MemberLevelScreen(navController = navController)
        }
        composable(Screen.PaymentMethodScreen.route) {
            PaymentMethodScreen(navController = navController)
        }

        composable(
            route = Screen.RideSelectionScreen.route,
            arguments = listOf(
                navArgument("dropPlaceId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("dropDescription") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("pickupPlaceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "current_location"
                },
                navArgument("pickupDescription") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "Your Current Location"
                }
            )
        ) {
            RideSelectionScreen(navController = navController)
        }
    }
}