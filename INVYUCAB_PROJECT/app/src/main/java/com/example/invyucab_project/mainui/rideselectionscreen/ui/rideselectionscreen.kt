package com.example.invyucab_project.mainui.rideselectionscreen.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowRight
// import androidx.compose.material.icons.filled.LocalOffer // ❌ REMOVED
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.domain.model.RideOption
import com.example.invyucab_project.domain.model.RideSelectionState
import com.example.invyucab_project.mainui.rideselectionscreen.viewmodel.RideSelectionViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
// import java.text.SimpleDateFormat // ❌ REMOVED
// import java.util.* // ❌ REMOVED
import java.lang.Exception


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSelectionScreen(
    navController: NavController,
    viewModel: RideSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(25.5941, 85.1376), 12f)
    }

    val context = LocalContext.current

    val mapStyleOptions = remember {
        try {
            val json = context.resources.openRawResource(R.raw.map_style_retro)
                .bufferedReader()
                .use { it.readText() }
            MapStyleOptions(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // This LaunchedEffect logic is correct and matches your file
    LaunchedEffect(uiState.pickupLocation, uiState.dropLocation, uiState.routePolyline) {
        val pickup = uiState.pickupLocation
        val drop = uiState.dropLocation

        if (uiState.routePolyline.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            uiState.routePolyline.forEach { boundsBuilder.include(it) }
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150)
                )
            } catch (e: IllegalStateException) {
                if (uiState.routePolyline.size == 1) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(uiState.routePolyline.first(), 15f))
                }
                e.printStackTrace()
            }
        } else if (pickup != null && drop != null) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(pickup)
            boundsBuilder.include(drop)
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150)
                )
            } catch (e: IllegalStateException) {
                if (pickup == drop) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pickup, 15f))
                }
                e.printStackTrace()
            }
        } else if (pickup != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pickup, 15f))
        }
    }

    Scaffold(
        containerColor = CabVeryLightMint
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false),
                properties = MapProperties(mapStyleOptions = mapStyleOptions)
            ) {

                // Initialize icons here, inside the map's content scope.
                val pickupIcon = remember(context) {
                    bitmapDescriptorFromDrawable(context, R.drawable.ic_pickup_marker)
                }
                val dropIcon = remember(context) {
                    bitmapDescriptorFromDrawable(context, R.drawable.ic_dropoff_marker)
                }

                if (uiState.routePolyline.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePolyline,
                        color = Color.Black,
                        width = 15f,
                        zIndex = 1f
                    )
                }

                uiState.pickupLocation?.let { pickupLatLng ->
                    Marker(
                        state = MarkerState(position = pickupLatLng),
                        title = "Pickup",
                        icon = pickupIcon // Use the icon initialized inside the map
                    )
                }

                uiState.dropLocation?.let { dropLatLng ->
                    Marker(
                        state = MarkerState(position = dropLatLng),
                        title = "Drop",
                        icon = dropIcon // Use the icon initialized inside the map
                    )
                }
            }

            LocationTopBar(
                pickup = uiState.pickupDescription,
                drop = uiState.dropDescription,
                onBack = { navController.navigateUp() },
                onFieldsClick = {
                    navController.popBackStack()
                }
            )

            RideOptionsBottomSheet(
                rideOptions = uiState.rideOptions
            )

            if (uiState.isLoading || uiState.isFetchingLocation) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 360.dp, start = 16.dp, end = 16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(text = error)
                }
            }
        }
    }
}

private fun bitmapDescriptorFromDrawable(
    context: Context,
    @DrawableRes drawableResId: Int
): BitmapDescriptor? {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate() ?: return null

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@Composable
fun LocationTopBar(
    pickup: String,
    drop: String,
    onBack: () -> Unit,
    onFieldsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CabMintGreen)
            .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            Canvas(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            ) {
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f,
                    pathEffect = pathEffect,
                    alpha = 0.7f
                )
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onFieldsClick)
        ) {
            Text(
                text = pickup,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 15.sp
            )
            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.3f))
            Text(
                text = drop,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.RideOptionsBottomSheet(rideOptions: List<RideOption>) {
    var selectedRideId by remember { mutableStateOf(1) }
    val areDetailsCalculated = rideOptions.isNotEmpty() && rideOptions.all { !it.isLoadingPrice }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetContent = {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                if (rideOptions.isEmpty()) {
                    Text("Loading ride options...", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(rideOptions) { ride ->
                            RideOptionItem(
                                ride = ride,
                                isSelected = ride.id == selectedRideId,
                                onClick = { selectedRideId = ride.id }
                            )
                        }
                    }
                }

                // ✅✅✅ START OF FIX ✅✅✅
                // Removed the "Offers" Row and its Arrangement.SpaceBetween
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                    // horizontalArrangement = Arrangement.SpaceBetween // ❌ REMOVED
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { /* TODO */ }) {
                        Icon(Icons.Default.CreditCard, contentDescription = "Payment", tint = Color.Gray)
                        Text("Cash", fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                    // ❌ REMOVED "Offers" Row
                }
                // ✅✅✅ END OF FIX ✅✅✅

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    enabled = areDetailsCalculated
                ) {
                    val selectedRideName = rideOptions.find { it.id == selectedRideId }?.name ?: "Ride"
                    Text("Book $selectedRideName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        sheetContainerColor = Color.White,
        sheetPeekHeight = 350.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {}
}

@Composable
fun RideOptionItem(ride: RideOption, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) CabMintGreen.copy(alpha = 0.1f) else Color.Transparent
    val borderModifier = if (isSelected) {
        Modifier.border(BorderStroke(3.dp, CabMintGreen), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(borderModifier)
            .background(backgroundColor)
            .padding(start = if(isSelected) 13.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ride.icon, contentDescription = ride.name, modifier = Modifier.size(48.dp))

        Spacer(modifier = Modifier.width(16.dp))

        Column( modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ride.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (ride.name.equals("Bike", ignoreCase = true)) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Person, contentDescription="Single Rider", modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text("1", fontSize=12.sp, color = Color.Gray)
                }
            }

            // ✅✅✅ START OF FIX ✅✅✅
            // Removed the Text composable that showed the drop-off time
            /*
            ride.estimatedDurationMinutes?.let { duration ->
                val dropTime = calculateDropOffTime(0, duration) // 0 ETA, just duration
                Text(
                    "Est. Drop $dropTime", // e.g., "Est. Drop 5:15 pm"
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            */
            // ✅✅✅ END OF FIX ✅✅✅
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            if (ride.isLoadingPrice) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = CabMintGreen
                )
            } else if (ride.price != null) {
                Text(
                    text = "${ride.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            ride.estimatedDistanceKm?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ❌❌❌ REMOVED unused function ❌❌❌
/*
fun calculateDropOffTime(etaMinutes: Int, durationMinutes: Int?): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, etaMinutes + (durationMinutes ?: 0))
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(calendar.time).lowercase()
}
*/