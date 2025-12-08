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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.invyucab_project.core.navigations.NavGraph
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appRepository: AppRepository

    // ✅ Permission Launcher for Android 13+
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

        // ✅ 1. Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // ✅ 2. Sync Token with Server
        fetchAndSyncFcmToken()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        val startDestination = Screen.SplashScreenLoggedIn.route

        setContent {
            INVYUCAB_PROJECTTheme {

                // ✅ 3. UI LISTENER FOR MESSAGES
                var showRideDialog by remember { mutableStateOf(false) }
                var currentMessageTitle by remember { mutableStateOf("") }
                var currentMessageBody by remember { mutableStateOf("") }

                // This listens for messages sent from MyFirebaseMessagingService
                LaunchedEffect(Unit) {
                    appRepository.fcmMessages.collect { message ->
                        currentMessageTitle = message.notification?.title ?: "New Update"
                        currentMessageBody = message.notification?.body ?: "You have a new message"
                        showRideDialog = true
                    }
                }

                // ✅ 4. THE POPUP DIALOG
                if (showRideDialog) {
                    AlertDialog(
                        onDismissRequest = { showRideDialog = false },
                        title = { Text(text = currentMessageTitle) },
                        text = { Text(text = currentMessageBody) },
                        confirmButton = {
                            TextButton(onClick = {
                                showRideDialog = false
                                // Optional: Handle "View" click to navigate
                            }) {
                                Text("View")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRideDialog = false }) {
                                Text("Dismiss")
                            }
                        }
                    )
                }

                // ✅ 5. MAIN NAVIGATION
                NavGraph(startDestination = startDestination)
            }
        }
    }

    // ✅ Helper function to sync token
    private fun fetchAndSyncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FCM", "Token retrieved: $token")

            // Save locally
            appRepository.saveFcmTokenLocally(token)

            // Sync with backend if user is logged in
            val phoneNumber = appRepository.getSavedPhoneNumber()
            if (!phoneNumber.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = appRepository.updateFcmToken(phoneNumber, token)
                        if (response.isSuccessful) {
                            Log.d("FCM", "Token synced with backend successfully")
                        } else {
                            Log.e("FCM", "Failed to sync token: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "Exception syncing token", e)
                    }
                }
            }
        }
    }
}