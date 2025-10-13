package com.example.invyucab_project.core.navigations

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object UserSignUpScreen : Screen("user_signup_screen")
    object DriverSignUpScreen : Screen("driver_signup_screen") // ✅ New route added
}