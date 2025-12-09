package com.example.invyucab_project.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.invyucab_project.R
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
// ✅ Import the new Activity
import com.example.invyucab_project.mainui.incomingride.IncomingRideActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var appRepository: AppRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        appRepository.saveFcmTokenLocally(token)
        appRepository.syncFcmToken()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Pass Message to UI (Foreground Handling via Flow)
        CoroutineScope(Dispatchers.Main).launch {
            appRepository.broadcastMessage(remoteMessage)
        }

        // 2. Handle Incoming Ride Request (Background/Lock Screen)
        // Check if the payload indicates a ride request
        if (remoteMessage.data.isNotEmpty()) {
            // Pass specific data keys
            val title = remoteMessage.data["title"] ?: "New Ride Request"
            val body = remoteMessage.data["body"] ?: "Incoming ride..."
            sendNotification(title, body, remoteMessage.data)
        }
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        // ✅ CHANGED: Point Intent to IncomingRideActivity
        val intent = Intent(this, IncomingRideActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Pass data to the Activity
            putExtra("pickup_lat", data["pickup_lat"])
            putExtra("pickup_lng", data["pickup_lng"])
            putExtra("pickup_address", data["pickup_address"])
            putExtra("price", data["price"])
            putExtra("distance", data["distance"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "invyu_ride_requests"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX for calls
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // ✅ IMPORTANT: This launches the full screen activity
            .setFullScreenIntent(pendingIntent, true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ride Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming ride alerts"
                enableVibration(true)
                // Set audio attributes to override Do Not Disturb if possible (depends on OS)
                setSound(defaultSoundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}