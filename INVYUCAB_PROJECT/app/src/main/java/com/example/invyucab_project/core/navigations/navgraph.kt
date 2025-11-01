package com.example.invyucab_project.core.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.invyucab_project.mainui.allservicesscreen.ui.AllServicesScreen
import com.example.invyucab_project.mainui.authscreen.ui.AuthScreen
import com.example.invyucab_project.mainui.homescreen.ui.HomeScreen
import com.example.invyucab_project.mainui.locationsearchscreen.ui.LocationSearchScreen
import com.example.invyucab_project.mainui.onboardingscreen.ui.OnboardingScreen
import com.example.invyucab_project.mainui.otpscreen.ui.OtpScreen
import com.example.invyucab_project.mainui.profilescreen.ui.ProfileScreen
import com.example.invyucab_project.mainui.rideselectionscreen.ui.RideSelectionScreen
import com.example.invyucab_project.mainui.travelscreen.ui.TravelScreen
import com.example.invyucab_project.mainui.userdetailsscreen.ui.UserDetailsScreen
import com.example.invyucab_project.mainui.profilescreen.editprofilescreen.ui.EditProfileScreen
import com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.ui.MemberLevelScreen
import com.example.invyucab_project.mainui.profilescreen.paymentmethodscreen.ui.PaymentMethodScreen
// ✅ ADDED: Imports for all new screens
import com.example.invyucab_project.mainui.adminscreen.ui.AdminScreen
import com.example.invyucab_project.mainui.driverdetailsscreen.ui.DriverDetailsScreen
import com.example.invyucab_project.mainui.driverscreen.ui.DriverScreen
import com.example.invyucab_project.mainui.roleselectionscreen.ui.RoleSelectionScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.OnboardingScreen.route
    ) {
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
                navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("gender") { type = NavType.StringType; nullable = true; defaultValue = null }, // ✅ ADDED
                navArgument("dob") { type = NavType.StringType; nullable = true; defaultValue = null }      // ✅ ADDED
            )
        ) {
            OtpScreen(navController = navController)
        }
        composable(
            route = Screen.UserDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            UserDetailsScreen(navController = navController)
        }

        // ✅ ADDED: Composable for RoleSelectionScreen
        composable(
            route = Screen.RoleSelectionScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("gender") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("dob") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            RoleSelectionScreen(navController = navController)
        }

        // ✅ ADDED: Composable for DriverDetailsScreen
        composable(
            route = Screen.DriverDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("gender") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("dob") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            DriverDetailsScreen(navController = navController)
        }

        // ✅ ADDED: Composable for AdminScreen
        composable(Screen.AdminScreen.route) {
            AdminScreen(navController = navController)
        }

        // ✅ ADDED: Composable for DriverScreen
        composable(Screen.DriverScreen.route) {
            DriverScreen(navController = navController)
        }

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
        composable(Screen.LocationSearchScreen.route) {
            LocationSearchScreen(navController = navController)
        }
        composable(
            route = Screen.RideSelectionScreen.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) {
            RideSelectionScreen(navController = navController)
        }
    }
}