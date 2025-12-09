package com.example.invyucab_project.mainui.incomingride

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingRideActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Wake up the screen (Turn on screen logic)
        turnOnScreen()

        // 2. Play Looping Ringtone
        playRingtone()

        // 3. Extract Data from Notification
        val pickupLat = intent.getStringExtra("pickup_lat")?.toDoubleOrNull() ?: 0.0
        val pickupLng = intent.getStringExtra("pickup_lng")?.toDoubleOrNull() ?: 0.0
        val pickupAddress = intent.getStringExtra("pickup_address") ?: "Unknown Location"
        val price = intent.getStringExtra("price") ?: "₹0"
        val distance = intent.getStringExtra("distance") ?: "0 km"

        setContent {
            INVYUCAB_PROJECTTheme {
                IncomingRideScreen(
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    pickupAddress = pickupAddress,
                    price = price,
                    distance = distance,
                    onAccept = {
                        stopRingtone()
                        // TODO: Call Accept API and Navigate to Navigation Screen
                        finish()
                    },
                    onDecline = {
                        stopRingtone()
                        // TODO: Call Decline API
                        finish()
                    }
                )
            }
        }
    }

    private fun turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun playRingtone() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer.create(this, notification)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }
}

@Composable
fun IncomingRideScreen(
    pickupLat: Double,
    pickupLng: Double,
    pickupAddress: String,
    price: String,
    distance: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val pickupLocation = LatLng(pickupLat, pickupLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Full Screen Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = pickupLocation),
                title = "Pickup Location",
                snippet = pickupAddress
            )
        }

        // 2. Bottom Sheet for Details & Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            // Price & Distance Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "New Ride Request", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = price,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = distance,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Address
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF00C853) // Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pickupAddress,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons (Accept / Decline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Decline Button
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), // Red
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Decline", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Accept Button
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), // Green
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}