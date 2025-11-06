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
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import kotlinx.coroutines.delay

@Composable
fun SplashScreenLoggedIn(navController: NavController) {
    // This effect runs once when the composable enters the screen
    LaunchedEffect(Unit) {
        // Wait for 1.5 seconds
        delay(1500L)
        // Navigate to HomeScreen
        navController.navigate(Screen.HomeScreen.route) {
            // Clear this splash screen from the back stack
            popUpTo(Screen.SplashScreenLoggedIn.route) {
                inclusive = true
            }
        }
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