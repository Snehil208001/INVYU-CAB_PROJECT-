package com.example.invyucab_project.mainui.incomingride

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.invyucab_project.MainActivity
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.data.repository.AppRepository
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IncomingRideActivity : ComponentActivity() {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var mediaPlayer: MediaPlayer? = null
    private var rideIdState = mutableIntStateOf(-1)

    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0
    private var pickupAddress: String = "Unknown"
    private var price: String = "₹0"
    private var distance: String = "0 km"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnOnScreen()
        playRingtone()

        processIntent(intent)

        if (rideIdState.intValue == -1) {
            fetchRideIdFromApi()
        }

        setContent {
            INVYUCAB_PROJECTTheme {
                val currentRideId = rideIdState.intValue

                IncomingRideScreen(
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    pickupAddress = pickupAddress,
                    price = price,
                    distance = distance,
                    onAccept = {
                        stopRingtone()
                        if (currentRideId != -1) {
                            handleAcceptRide(currentRideId)
                        } else {
                            fetchRideIdFromApi()
                            Toast.makeText(this, "Finding Ride ID... Try again", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDecline = {
                        stopRingtone()
                        if (currentRideId != -1) {
                            handleDeclineRide(currentRideId)
                        } else {
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processIntent(intent)
        if (rideIdState.intValue == -1) {
            fetchRideIdFromApi()
        }
    }

    private fun processIntent(intent: Intent) {
        pickupLat = intent.getStringExtra("pickup_lat")?.toDoubleOrNull() ?: 0.0
        pickupLng = intent.getStringExtra("pickup_lng")?.toDoubleOrNull() ?: 0.0
        pickupAddress = intent.getStringExtra("pickup_address") ?: "Unknown Location"
        price = intent.getStringExtra("price") ?: "₹0"
        distance = intent.getStringExtra("distance") ?: "0 km"

        var id = intent.getStringExtra("ride_id")?.toIntOrNull() ?: -1
        if (id == -1) id = intent.getIntExtra("ride_id", -1)
        if (id == -1) id = intent.getStringExtra("rideId")?.toIntOrNull() ?: -1

        rideIdState.intValue = id
    }

    private fun fetchRideIdFromApi() {
        lifecycleScope.launch {
            val driverId = userPreferencesRepository.getUserId()?.toIntOrNull() ?: return@launch
            try {
                val response = appRepository.getDriverUpcomingRides(driverId, pickupLat, pickupLng)
                if (response.isSuccessful && response.body()?.success == true) {
                    val rides = response.body()?.data
                    val pendingRide = rides?.firstOrNull { it.status == "requested" }
                    if (pendingRide != null && pendingRide.rideId != null) {
                        rideIdState.intValue = pendingRide.rideId!!
                    }
                }
            } catch (e: Exception) {
                Log.e("IncomingRide", "API Fallback Error", e)
            }
        }
    }

    private fun handleAcceptRide(rideId: Int) {
        lifecycleScope.launch {
            val driverId = userPreferencesRepository.getUserId()?.toIntOrNull()
            if (driverId == null) {
                Toast.makeText(this@IncomingRideActivity, "Driver not logged in", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            try {
                // ✅ Prevent future notifications immediately
                appRepository.markRideProcessed(rideId)

                val response = appRepository.acceptRide(rideId, driverId)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@IncomingRideActivity, "Ride Accepted!", Toast.LENGTH_SHORT).show()

                    // ✅ Notify ViewModel to switch tabs
                    appRepository.triggerRideAcceptedNavigation()

                    // ✅ FIX: Use REORDER_TO_FRONT to bring existing MainActivity to top (Prevents Restart)
                    val mainIntent = Intent(this@IncomingRideActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(mainIntent)
                    finish()
                } else {
                    val error = response.body()?.message ?: "Accept failed"
                    Toast.makeText(this@IncomingRideActivity, error, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("IncomingRide", "Accept Error", e)
                Toast.makeText(this@IncomingRideActivity, "Network Error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleDeclineRide(rideId: Int) {
        lifecycleScope.launch {
            try {
                // ✅ Mark processed so we don't see it again
                appRepository.markRideProcessed(rideId)
                Toast.makeText(this@IncomingRideActivity, "Declining...", Toast.LENGTH_SHORT).show()
                appRepository.updateRideStatus(rideId, "cancelled")
            } catch (e: Exception) {
                Log.e("IncomingRide", "Decline Error", e)
            } finally {
                finish()
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
            @Suppress("DEPRECATION")
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
            var notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            mediaPlayer = MediaPlayer.create(this, notification)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    var isProcessing by remember { mutableStateOf(false) }
    val pickupLocation = LatLng(pickupLat, pickupLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 15f)
    }

    LaunchedEffect(pickupLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(pickupLocation, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "New Ride Request", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = if (price.startsWith("₹")) price else "₹$price",
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

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF00C853)
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

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { isProcessing = true; onDecline() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Decline", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isProcessing = true; onAccept() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.weight(1f).height(56.dp),
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
}