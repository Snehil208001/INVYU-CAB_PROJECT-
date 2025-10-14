package com.example.invyucab_project.core.navigations

sealed class Screen(val route: String) {
    object OnboardingScreen : Screen("onboarding_screen")
    object AuthScreen : Screen("auth_screen")
    // ✅ Add the new OTP screen route with a phone argument
    object OtpScreen : Screen("otp_screen/{phone}") {
        // Helper function to create the route with the actual phone number
        fun createRoute(phone: String) = "otp_screen/$phone"
    }
}