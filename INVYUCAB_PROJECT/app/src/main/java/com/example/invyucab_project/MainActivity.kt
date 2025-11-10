package com.example.invyucab_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle // ✅ ADD THIS IMPORT
import com.example.invyucab_project.core.navigations.NavGraph
import com.example.invyucab_project.core.navigations.Screen
// import com.example.invyucab_project.data.preferences.UserPreferencesRepository // ❌ No longer needed here
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import dagger.hilt.android.AndroidEntryPoint
// import javax.inject.Inject // ❌ No longer needed here

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ❌ This logic is now moved to SplashScreenViewModel
    // @Inject
    // lateinit var userPreferencesRepository: UserPreferencesRepository

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
        // The app will ALWAYS start at the SplashScreenLoggedIn.
        // This screen will then decide where to go (Onboarding or Home).
        val startDestination = Screen.SplashScreenLoggedIn.route
        // ✅✅✅ END OF MODIFICATION ✅✅✅

        setContent {
            INVYUCAB_PROJECTTheme {
                NavGraph(startDestination = startDestination) // ✅ PASS the single start route
            }
        }
    }
}