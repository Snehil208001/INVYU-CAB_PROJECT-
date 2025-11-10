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
import com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel.SplashScreenViewModel
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import kotlinx.coroutines.delay

@Composable
fun SplashScreenLoggedIn(
    navController: NavController,
    viewModel: SplashScreenViewModel = hiltViewModel() // ✅ Inject ViewModel
) {
    // This effect runs once when the composable enters the screen
    LaunchedEffect(Unit) {
        // Wait for 1.5 seconds
        delay(1500L)

        // ✅✅✅ START OF MODIFICATION ✅✅✅
        // Check if the user is logged in
        val isUserLoggedIn = viewModel.isUserLoggedIn()

        // Determine the next destination
        val nextDestination = if (isUserLoggedIn) {
            Screen.HomeScreen.route // User is logged in
        } else {
            Screen.OnboardingScreen.route // User is NOT logged in
        }

        // Navigate to the correct destination
        navController.navigate(nextDestination) {
            // Clear this splash screen from the back stack
            popUpTo(Screen.SplashScreenLoggedIn.route) {
                inclusive = true
            }
        }
        // ✅✅✅ END OF MODIFICATION ✅✅✅
    }

    Scaffold(
        containerColor = CabVeryLightMint // Use the light app background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Display the logo
            Image(
                painter = painterResource(id = R.drawable.logo_auth),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}