package com.example.invyucab_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle // ✅ ADD THIS IMPORT
import com.example.invyucab_project.core.navigations.NavGraph
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅✅✅ THIS IS THE FIX ✅✅✅
        // Force the status bar icons to be dark (for light backgrounds)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        // ✅✅✅ END OF FIX ✅✅✅

        // ✅✅✅ START OF MODIFICATION ✅✅✅
        // This logic is now simpler.
        val isUserLoggedIn = userPreferencesRepository.getUserStatus() == "active"

        val startDestination = when {
            // Priority 1: If user is logged in, go to the logged-in splash.
            isUserLoggedIn -> Screen.SplashScreenLoggedIn.route
            // Priority 2: If user is NOT logged in, always show Onboarding.
            else -> Screen.OnboardingScreen.route
        }
        // ✅✅✅ END OF MODIFICATION ✅✅✅

        setContent {
            INVYUCAB_PROJECTTheme {
                NavGraph(startDestination = startDestination) // ✅ PASS the dynamic start route
            }
        }
    }
}