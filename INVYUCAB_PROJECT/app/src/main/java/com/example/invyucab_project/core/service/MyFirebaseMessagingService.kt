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

        Log.e("FCM_DEBUG", "--- MESSAGE RECEIVED ---")
        Log.e("FCM_DEBUG", "From: ${remoteMessage.from}")
        Log.e("FCM_DEBUG", "Raw Data Map: ${remoteMessage.data}")

        // 1. Pass Message to UI (Foreground Handling via Flow)
        CoroutineScope(Dispatchers.Main).launch {
            appRepository.broadcastMessage(remoteMessage)
        }

        // 2. Handle Incoming Ride Request (Background/Lock Screen)
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data

            // Debugging: Check specifically for ID
            val hasId = data.containsKey("ride_id") || data.containsKey("rideId")
            if (!hasId) {
                Log.e("FCM_DEBUG", "CRITICAL ERROR: Backend payload is MISSING ride_id!")
            } else {
                Log.e("FCM_DEBUG", "Ride ID found in payload: ${data["ride_id"] ?: data["rideId"]}")
            }

            val title = data["title"] ?: "New Ride Request"
            val body = data["body"] ?: "Incoming ride..."

            sendNotification(title, body, data)
        }
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val intent = Intent(this, IncomingRideActivity::class.java)

        // Clear flags to ensure we get a fresh activity state
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // 1. Pass ALL data keys blindly
        data.forEach { (key, value) ->
            intent.putExtra(key, value)
        }

        // 2. FORCE extract ride_id and put it specifically as "ride_id"
        // This handles "rideId", "ride_id", "id" variations from backend
        val rideId = data["ride_id"] ?: data["rideId"] ?: data["id"]
        if (rideId != null) {
            intent.putExtra("ride_id", rideId)
            Log.e("FCM_DEBUG", "Attaching ride_id to Intent: $rideId")
        } else {
            Log.e("FCM_DEBUG", "Cannot attach ride_id: Value is NULL")
        }

        // 3. Unique Request Code (Time-based) to prevent PendingIntent reuse/caching
        val uniqueRequestCode = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            uniqueRequestCode,
            intent,
            // FLAG_UPDATE_CURRENT is critical here
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
                setSound(defaultSoundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(uniqueRequestCode, notificationBuilder.build())
    }
}