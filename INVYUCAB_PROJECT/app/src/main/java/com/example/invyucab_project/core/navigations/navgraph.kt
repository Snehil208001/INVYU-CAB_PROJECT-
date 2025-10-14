package com.example.invyucab_project.core.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.invyucab_project.mainui.authscreen.ui.AuthScreen
import com.example.invyucab_project.mainui.onboardingscreen.ui.OnboardingScreen
import com.example.invyucab_project.mainui.otpscreen.ui.OtpScreen // ✅ Import OTP Screen

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
        // ✅ Add the composable for the OTP screen
        composable(
            route = Screen.OtpScreen.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            OtpScreen(navController = navController)
        }
    }
}