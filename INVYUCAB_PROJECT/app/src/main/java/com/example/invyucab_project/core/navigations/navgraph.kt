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
import com.example.invyucab_project.mainui.rideselectionscreen.ui.RideSelectionScreen // ✅ ADDED
import com.example.invyucab_project.mainui.travelscreen.ui.TravelScreen
import com.example.invyucab_project.mainui.userdetailsscreen.ui.UserDetailsScreen

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
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            OtpScreen(navController = navController)
        }
        composable(
            route = Screen.UserDetailsScreen.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            UserDetailsScreen(navController = navController)
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

        composable(Screen.LocationSearchScreen.route) {
            LocationSearchScreen(navController = navController)
        }

        // ✅ ADDED: Composable for the new RideSelectionScreen
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