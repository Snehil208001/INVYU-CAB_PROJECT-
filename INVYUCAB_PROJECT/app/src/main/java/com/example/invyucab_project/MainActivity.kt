package com.example.invyucab_project

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.invyucab_project.core.common.RideNotificationObserver
import com.example.invyucab_project.core.navigations.NavGraph
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appRepository: AppRepository

    // Permission Launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.d("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 2. Sync Token with Server
        appRepository.syncFcmToken()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        val startDestination = Screen.SplashScreenLoggedIn.route

        setContent {
            INVYUCAB_PROJECTTheme {

                // 3. UI LISTENER FOR MESSAGES (Refactored)
                // We now call the separate observer composable
                RideNotificationObserver(
                    appRepository = appRepository,
                    onAccept = {
                        // TODO: Add logic to Navigate to Driver Screen or Accept API
                    },
                    onDecline = {
                        // TODO: Add logic to call Decline API
                    }
                )

                // 4. MAIN NAVIGATION
                NavGraph(startDestination = startDestination)
            }
        }
    }
}