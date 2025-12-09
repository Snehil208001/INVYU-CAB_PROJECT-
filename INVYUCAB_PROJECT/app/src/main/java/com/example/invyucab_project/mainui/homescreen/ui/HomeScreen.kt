package com.example.invyucab_project.mainui.homescreen.ui

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.domain.model.AutocompletePrediction
import com.example.invyucab_project.domain.model.RecentRide
import com.example.invyucab_project.domain.model.SearchField
import com.example.invyucab_project.mainui.homescreen.viewmodel.HomeViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.example.invyucab_project.ui.theme.LightSlateGray
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val activity = (LocalContext.current as? Activity)
    BackHandler {
        activity?.finish()
    }

    val apiError by viewModel.apiError
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getCurrentLocation()
                viewModel.fetchRecentRides()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val context = LocalContext.current

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

    val showPermissionBanner = !locationPermissionsState.allPermissionsGranted && permissionRequestLaunched

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
                snackbarHostState.showSnackbar(message)
            }
            viewModel.clearApiError()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    val isButtonEnabled =
        !uiState.pickupPlaceId.isNullOrBlank() && !uiState.dropPlaceId.isNullOrBlank()

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Book a Ride", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabVeryLightMint,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .imePadding()
            ) {
                AnimatedVisibility(
                    visible = isButtonEnabled,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    Button(
                        onClick = { viewModel.onContinueClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    ) {
                        Text(
                            "Continue",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                AppBottomNavigation(navController = navController, selectedItem = "Home")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        ) {

            AnimatedVisibility(visible = showPermissionBanner) {
                LocationPermissionBanner(
                    onAllowClick = onAllowClick
                )
            }

            SearchInputSection(
                pickupQuery = uiState.pickupQuery,
                dropQuery = uiState.dropQuery,
                activeField = uiState.activeField,
                onFieldActivated = viewModel::onFocusChange,
                onFieldFocusLost = viewModel::onFocusLost,
                onQueryChanged = { query ->
                    if (uiState.activeField == SearchField.PICKUP) {
                        viewModel.onPickupQueryChange(query)
                    } else {
                        viewModel.onDropQueryChange(query)
                    }
                },
                onClearField = { field ->
                    if (field == SearchField.PICKUP) {
                        viewModel.onClearPickup()
                    } else {
                        viewModel.onClearDrop()
                    }
                }
            )

            val results = if (uiState.activeField == SearchField.PICKUP) {
                uiState.pickupResults
            } else {
                uiState.dropResults
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (results.isNotEmpty()) {
                    items(results) { prediction ->
                        PredictionItem(
                            prediction = prediction,
                            onClick = {
                                viewModel.onPredictionTapped(prediction)
                            }
                        )
                    }
                } else {
                    if (uiState.recentRides.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Rides",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                            )
                        }
                        items(uiState.recentRides) { ride ->
                            RecentRideItem(
                                ride = ride,
                                onClick = {
                                    // ✅ FIXED: Use createRoute helper to handle encoding and params safely.
                                    val route = Screen.RideSelectionScreen.createRoute(
                                        dropPlaceId = "", // Pass empty for Recent Rides (no PlaceID)
                                        dropDescription = ride.dropAddress,
                                        pickupPlaceId = "",
                                        pickupDescription = ride.pickupAddress,
                                        pickupLat = ride.pickupLat,
                                        pickupLng = ride.pickupLng,
                                        dropLat = ride.dropLat,
                                        dropLng = ride.dropLng
                                    )
                                    navController.navigate(route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentRideItem(ride: RecentRide, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = CabMintGreen,
                modifier = Modifier
                    .size(40.dp)
                    .background(CabVeryLightMint, CircleShape)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Pickup Row Removed - Only showing Dropoff Location
            Text(
                text = ride.dropAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
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
            .background(Color(0xFFFFFBE6)) // Light yellow background
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "To see prices and availability, turn on location.",
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
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester,
    onFocusChanged: (isFocused: Boolean) -> Unit,
    onClear: () -> Unit,
    isFocused: Boolean
) {
    val borderColor = if (isFocused) CabMintGreen else Color.Gray.copy(alpha = 0.5f)
    val pickupTextColor = if (value == "Your Current Location") CabMintGreen else Color.Black

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = pickupTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        cursorBrush = SolidColor(CabMintGreen),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                innerTextField()
                if (value.isNotEmpty() && isFocused && value != "Your Current Location") {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { onClear() }
                    )
                }
            }
        }
    )
}

@Composable
fun SearchInputSection(
    pickupQuery: String,
    dropQuery: String,
    activeField: SearchField,
    onFieldActivated: (SearchField) -> Unit,
    onFieldFocusLost: (SearchField) -> Unit,
    onQueryChanged: (String) -> Unit,
    onClearField: (SearchField) -> Unit
) {
    val pickupFocusRequester = remember { FocusRequester() }
    val dropFocusRequester = remember { FocusRequester() }

    val isInitialRun = remember { mutableStateOf(true) }

    LaunchedEffect(activeField) {
        if (isInitialRun.value) {
            isInitialRun.value = false
        } else {
            if (activeField == SearchField.PICKUP) {
                pickupFocusRequester.requestFocus()
            } else if (activeField == SearchField.DROP) {
                dropFocusRequester.requestFocus()
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CabVeryLightMint)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        LocationConnectorGraphic()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            StyledTextField(
                value = pickupQuery,
                onValueChange = onQueryChanged,
                placeholder = "Enter pickup location",
                focusRequester = pickupFocusRequester,
                onFocusChanged = { isFocused ->
                    if (isFocused) {
                        onFieldActivated(SearchField.PICKUP)
                    } else {
                        onFieldFocusLost(SearchField.PICKUP)
                    }
                },
                onClear = { onClearField(SearchField.PICKUP) },
                isFocused = activeField == SearchField.PICKUP
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = dropQuery,
                onValueChange = onQueryChanged,
                placeholder = "Enter drop location",
                focusRequester = dropFocusRequester,
                onFocusChanged = { isFocused ->
                    if (isFocused) {
                        onFieldActivated(SearchField.DROP)
                    } else {
                        onFieldFocusLost(SearchField.DROP)
                    }
                },
                onClear = { onClearField(SearchField.DROP) },
                isFocused = activeField == SearchField.DROP
            )
        }
    }
}

@Composable
fun LocationConnectorGraphic() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(CabMintGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(CabMintGreen)
            )
        }

        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        Canvas(
            modifier = Modifier
                .height(36.dp)
                .width(1.dp)
        ) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2f,
                pathEffect = pathEffect
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFA500).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFA500))
            )
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = "Location",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LightSlateGray)
                .padding(8.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.primaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = prediction.secondaryText,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}