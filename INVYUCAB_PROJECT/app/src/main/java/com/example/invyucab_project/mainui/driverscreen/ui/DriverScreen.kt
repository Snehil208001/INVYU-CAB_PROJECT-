package com.example.invyucab_project.mainui.driverscreen.ui

import android.util.Log
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class)
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

    val selectedBottomNavItem = "Rides"

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
                // ✅ --- THIS IS THE FIX ---
                // Add an else branch to make the 'when' exhaustive
                else -> {}
                // ✅ --- END OF FIX ---
            }
        }
    }

    // --- Error Handling for GPS ---
    LaunchedEffect(apiError) {
        if (apiError != null) {
            snackbarHostState.showSnackbar(
                message = apiError!!,
                duration = SnackbarDuration.Short
            )
            viewModel.clearApiError()
        }
    }

    // --- Map Camera State ---
    val defaultLocation = LatLng(25.5941, 85.1376) // Patna (fallback)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // --- Animate Map to Current Location ---
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 16f)
            )
        }
    }

    // --- Load Custom Map Style ---
    val context = LocalContext.current
    val mapStyleOptions = remember {
        try {
            val json = context.resources
                .openRawResource(R.raw.map_style_retro)
                .bufferedReader()
                .use { it.readText() }
            MapStyleOptions(json)
        } catch (e: Exception) {
            Log.e("DriverScreen", "Can't load map style", e)
            null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !cameraPositionState.isMoving,
        drawerContent = {
            ModalDrawerSheet {
                // TODO: Replace with actual driver info from ViewModel
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
                        viewModel.onLogoutClicked() // Call logout function
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
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = mapStyleOptions
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                        zoomControlsEnabled = false
                    )
                )

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