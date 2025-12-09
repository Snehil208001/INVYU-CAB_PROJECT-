package com.example.invyucab_project.core.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.invyucab_project.data.repository.AppRepository

@Composable
fun RideNotificationObserver(
    appRepository: AppRepository,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {}
) {
    // UI State for the Dialog
    var showRideDialog by remember { mutableStateOf(false) }
    var currentMessageTitle by remember { mutableStateOf("") }
    var currentMessageBody by remember { mutableStateOf("") }

    // Listen for FCM messages from the Repository
    LaunchedEffect(Unit) {
        appRepository.fcmMessages.collect { message ->
            // Prioritize 'data' payload for background handling
            val title = message.data["title"] ?: message.notification?.title ?: "New Ride Request"
            val body = message.data["body"] ?: message.notification?.body ?: "You have a new ride request."

            currentMessageTitle = title
            currentMessageBody = body
            showRideDialog = true
        }
    }

    // Show the Dialog when state is true
    if (showRideDialog) {
        AlertDialog(
            onDismissRequest = { showRideDialog = false },
            title = { Text(text = currentMessageTitle) },
            text = { Text(text = currentMessageBody) },
            confirmButton = {
                TextButton(onClick = {
                    showRideDialog = false
                    onAccept()
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRideDialog = false
                    onDecline()
                }) {
                    Text("Decline", color = Color.Red)
                }
            }
        )
    }
}