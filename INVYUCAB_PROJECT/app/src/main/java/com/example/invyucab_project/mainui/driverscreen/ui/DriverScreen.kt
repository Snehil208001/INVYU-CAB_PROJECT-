package com.example.invyucab_project.mainui.driverscreen.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import com.example.invyucab_project.R
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.driverscreen.viewmodel.DriverViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DriverScreen(
    navController: NavController,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val isActive by viewModel.isActive.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState() // Kept if needed for other logic, though map is gone
    val apiError by viewModel.apiError
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val showVehicleBanner by viewModel.showVehicleBanner.collectAsState()

    val selectedBottomNavItem = "Rides"

    val context = LocalContext.current

    // --- ✅ START: ADDED CODE FOR SYSTEM BACK BUTTON ---
    val activity = (LocalContext.current as? Activity)
    BackHandler {
        activity?.finish()
    }
    // --- ✅ END: ADDED CODE FOR SYSTEM BACK BUTTON ---

    // --- ✅ START OF PERMISSION LOGIC ---

    // --- 1. Launcher for App PERMISSIONS (App Settings) ---
    val permissionSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.getCurrentLocation()
    }

    // --- 2. Launcher for Location SERVICES (GPS Toggle) ---
    val locationServiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.getCurrentLocation()
    }

    // --- 3. State for App PERMISSIONS ---
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

    // --- 4. Click Handler for App PERMISSION Banner ---
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
    // --- ✅ END OF PERMISSION LOGIC ---


    // ✅ --- START: LIFECYCLE OBSERVER ---
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
    // ✅ --- END: LIFECYCLE OBSERVER ---

    // --- Event Collection for Navigation ---
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

    // --- ✅ START: FULL Error Handling (FOR GPS SERVICE) ---
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
    // --- ✅ END: FULL Error Handling ---

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true, // Enabled gestures since map is gone
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
                        // navController.navigate("earnings_screen_route")
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
                    onSearchClicked = { /* TODO: Implement search functionality */ }
                )
            },
            bottomBar = {
                DriverBottomBar(
                    selectedItem = selectedBottomNavItem,
                    onMyRidesClicked = {
                        // Already on this screen, do nothing or refresh
                    },
                    onTripClicked = {
                        // TODO: Navigate to "Current Trip" or "Trip History" screen
                    },
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
                // Map Removed

                // ✅ WRAP BANNERS IN A COLUMN
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)) {

                    // --- ✅ LOCATION PERMISSION BANNER ---
                    AnimatedVisibility(
                        visible = showLocationPermissionBanner,
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut(),
                    ) {
                        LocationPermissionBanner(
                            onAllowClick = onAllowClick
                        )
                    }

                    // --- VEHICLE BANNER ---
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
                }

                // Status Indicator
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

// --- ✅ LOCATION PERMISSION BANNER ---
@Composable
fun LocationPermissionBanner(
    onAllowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBE6)) // Light yellow background
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


// --- ✅ VEHICLE BANNER ---
@Composable
fun VehicleBanner(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBE6)) // Light yellow
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFF57F17) // Amber
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
    onMyRidesClicked: () -> Unit,
    onTripClicked: () -> Unit,
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
                text = "Rides",
                icon = Icons.Default.DirectionsCar,
                isSelected = selectedItem == "Rides",
                onClick = onMyRidesClicked
            )
            BottomNavItem(
                text = "Trip",
                icon = Icons.Default.Star,
                isSelected = selectedItem == "Trip",
                onClick = onTripClicked
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
            .padding(horizontal = 16.dp)
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
            color = if (isSelected) activeColor else inactiveColor
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