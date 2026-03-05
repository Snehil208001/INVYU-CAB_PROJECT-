package com.example.invyucab_project.mainui.splashscreen_loggedin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel.SplashDestination
import com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel.SplashScreenViewModel
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@Composable
fun SplashScreenLoggedIn(
    navController: NavController,
    viewModel: SplashScreenViewModel = hiltViewModel()
) {
    // Trigger the check logic on startup
    LaunchedEffect(Unit) {
        viewModel.checkStartDestination()

        viewModel.navigationEvent.collect { destination ->
            val route = when (destination) {
                is SplashDestination.Onboarding -> Screen.OnboardingScreen.route
                is SplashDestination.Auth -> Screen.AuthScreen.route
                is SplashDestination.Home -> Screen.HomeScreen.route
                is SplashDestination.Driver -> Screen.DriverScreen.route
                is SplashDestination.Admin -> Screen.AdminScreen.route

                // ✅ Handle Active Ride Navigation
                is SplashDestination.BookingDetail -> {
                    val ride = destination.ride
                    Screen.BookingDetailScreen.createRoute(
                        driverName = ride.driverName ?: "Driver",
                        vehicleModel = ride.model ?: "Vehicle",
                        otp = ride.userPin?.toString() ?: "0000",
                        rideId = ride.rideId ?: 0,
                        riderId = ride.riderId ?: 0,
                        driverId = ride.driverId ?: 0,
                        role = "rider",
                        pickupLat = ride.pickupLatitude?.toDoubleOrNull() ?: 0.0,
                        pickupLng = ride.pickupLongitude?.toDoubleOrNull() ?: 0.0,
                        dropLat = ride.dropLatitude?.toDoubleOrNull() ?: 0.0,
                        dropLng = ride.dropLongitude?.toDoubleOrNull() ?: 0.0
                    )
                }
            }

            // Navigate and remove Splash from backstack
            navController.navigate(route) {
                popUpTo(Screen.SplashScreenLoggedIn.route) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        containerColor = CabVeryLightMint
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.invyucablogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}