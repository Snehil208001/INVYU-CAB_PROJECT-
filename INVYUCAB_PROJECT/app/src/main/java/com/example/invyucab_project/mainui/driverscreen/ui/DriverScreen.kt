package com.example.invyucab_project.mainui.driverscreen.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.driverscreen.viewmodel.DriverViewModel
import com.example.invyucab_project.mainui.driverscreen.viewmodel.RideHistoryUiModel
import com.example.invyucab_project.mainui.driverscreen.viewmodel.RideRequestItem
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DriverScreen(
    navController: NavController,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val isActive by viewModel.isActive.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val apiError by viewModel.apiError
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val showVehicleBanner by viewModel.showVehicleBanner.collectAsState()
    // Observe ride requests
    val rideRequests by viewModel.rideRequests.collectAsState()

    // Observe Total Rides History
    val totalRides by viewModel.totalRides.collectAsState()

    var selectedBottomNavItem by remember { mutableStateOf("Upcoming") }

    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    BackHandler {
        activity?.finish()
    }

    // --- PERMISSION LOGIC ---
    val permissionSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.getCurrentLocation()
    }

    val locationServiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.getCurrentLocation()
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var permissionRequestLaunched by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
            permissionRequestLaunched = true
        }
    }

    LaunchedEffect(key1 = locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.getCurrentLocation()
        }
    }

    val showLocationPermissionBanner = !locationPermissionsState.allPermissionsGranted && permissionRequestLaunched

    val onAllowClick: () -> Unit = {
        if (locationPermissionsState.shouldShowRationale) {
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            permissionSettingsLauncher.launch(intent)
        }
    }

    // --- LIFECYCLE OBSERVER ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkVehicleDetails()

                if (locationPermissionsState.allPermissionsGranted) {
                    viewModel.getCurrentLocation()
                } else {
                    permissionRequestLaunched = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- Fetch Total Rides when tab changes ---
    LaunchedEffect(selectedBottomNavItem) {
        if (selectedBottomNavItem == "Total") {
            viewModel.fetchTotalRides()
        }
    }

    // --- Event Collection ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(Screen.DriverScreen.route) { inclusive = true }
                    }
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    // --- ERROR HANDLING ---
    LaunchedEffect(apiError) {
        apiError?.let { message ->
            if (message.contains("location services (GPS)")) {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Turn On",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationServiceLauncher.launch(intent)
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearApiError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(
                    driverName = "Snehil",
                    driverRating = "4.9",
                    profileImageUrl = "https://example.com/driver_profile.jpg"
                )
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Earnings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Divider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.onLogoutClicked()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DriverTopAppBar(
                    isActive = isActive,
                    onToggleChanged = { viewModel.onActiveToggleChanged(it) },
                    onProfileClicked = { scope.launch { drawerState.open() } },
                    onSearchClicked = { /* TODO */ }
                )
            },
            bottomBar = {
                DriverBottomBar(
                    selectedItem = selectedBottomNavItem,
                    onOngoingRidesClicked = { selectedBottomNavItem = "Ongoing" },
                    onUpcomingRidesClicked = { selectedBottomNavItem = "Upcoming" },
                    onTotalRidesClicked = { selectedBottomNavItem = "Total" },
                    onProfileClicked = {
                        navController.navigate(Screen.DriverProfileScreen.route)
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                Column(modifier = Modifier.fillMaxSize()) {

                    // --- BANNERS (Always visible if conditions met) ---
                    AnimatedVisibility(
                        visible = showLocationPermissionBanner,
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut(),
                    ) {
                        LocationPermissionBanner(onAllowClick = onAllowClick)
                    }

                    AnimatedVisibility(
                        visible = showVehicleBanner,
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut(),
                    ) {
                        VehicleBanner(
                            onClick = {
                                navController.navigate(Screen.VehiclePreferencesScreen.route)
                            }
                        )
                    }

                    // --- CONTENT SWITCHING BASED ON TAB ---
                    when (selectedBottomNavItem) {
                        "Ongoing" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No ongoing rides", color = Color.Gray)
                            }
                        }

                        "Upcoming" -> {
                            if (isActive) {
                                if (rideRequests.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Looking for rides...", color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(bottom = 100.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(rideRequests) { ride ->
                                            // ✅ Updated Call: Removed onDecline
                                            RideRequestCard(
                                                ride = ride,
                                                onAccept = { viewModel.onAcceptRide(ride) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("You are offline. Go Active to see rides.", color = Color.Gray)
                                }
                            }
                        }

                        "Total" -> {
                            if (totalRides.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No ride history found.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 100.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(totalRides) { ride ->
                                        RideHistoryCard(ride = ride)
                                    }
                                }
                            }
                        }

                        else -> {
                            // Fallback
                        }
                    }
                }

                // Status Indicator (Bottom)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(
                                color = if (isActive) CabMintGreen else Color.Gray,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = "Status icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isActive) "You're Active" else "You're Inactive",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// --- ✅ UPDATED: Ride Request Card UI (Removed Decline Button) ---
@Composable
fun RideRequestCard(
    ride: RideRequestItem,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "New Ride Request!",
                style = MaterialTheme.typography.titleMedium,
                color = CabMintGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Pickup: ${ride.pickupAddress}", fontWeight = FontWeight.SemiBold)
            Text("Drop: ${ride.dropAddress}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${ride.price}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Only Accept Button remaining
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accept", color = Color.White)
            }
        }
    }
}

// --- Ride History Card UI ---
@Composable
fun RideHistoryCard(ride: RideHistoryUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ride #${ride.rideId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = ride.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Pickup: ${ride.pickup}", style = MaterialTheme.typography.bodyMedium)
            Text("Drop: ${ride.drop}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${ride.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Surface(
                    color = if (ride.status.equals("completed", ignoreCase = true)) CabMintGreen else Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = ride.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun LocationPermissionBanner(
    onAllowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBE6))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "To go online and receive rides, turn on location.",
            modifier = Modifier.weight(1f),
            color = Color.Black,
            fontSize = 14.sp
        )

        Spacer(Modifier.width(8.dp))

        TextButton(onClick = onAllowClick) {
            Text(
                "ALLOW",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VehicleBanner(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBE6))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFF57F17)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "No vehicle registered. Tap to add your vehicle.",
            color = Color.Black.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverTopAppBar(
    isActive: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onProfileClicked: () -> Unit,
    onSearchClicked: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onProfileClicked) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu",
                    tint = Color.White
                )
            }
        },
        title = {
            Text(
                text = if (isActive) "Active" else "Inactive",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Switch(
                checked = isActive,
                onCheckedChange = onToggleChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
                )
            )
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CabMintGreen,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
    )
}

@Composable
private fun DriverBottomBar(
    selectedItem: String,
    onOngoingRidesClicked: () -> Unit,
    onUpcomingRidesClicked: () -> Unit,
    onTotalRidesClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                text = "Ongoing",
                icon = Icons.Default.DirectionsCar,
                isSelected = selectedItem == "Ongoing",
                onClick = onOngoingRidesClicked
            )
            BottomNavItem(
                text = "Upcoming",
                icon = Icons.Default.Schedule,
                isSelected = selectedItem == "Upcoming",
                onClick = onUpcomingRidesClicked
            )
            BottomNavItem(
                text = "Total",
                icon = Icons.Default.History,
                isSelected = selectedItem == "Total",
                onClick = onTotalRidesClicked
            )
            BottomNavItem(
                text = "Profile",
                icon = Icons.Default.Person,
                isSelected = selectedItem == "Profile",
                onClick = onProfileClicked
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = CabMintGreen
    val inactiveColor = Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) activeColor else inactiveColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.sp
        )
    }
}

@Composable
fun DrawerHeader(driverName: String, driverRating: String, profileImageUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CabMintGreen)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(profileImageUrl),
            contentDescription = "Driver Profile",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = driverName,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color.Yellow)
            Text(
                text = driverRating,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    INVYUCAB_PROJECTTheme {
        DriverScreen(navController = rememberNavController())
    }
}