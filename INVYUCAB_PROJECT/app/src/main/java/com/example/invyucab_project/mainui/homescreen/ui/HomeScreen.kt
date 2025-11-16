package com.example.invyucab_project.mainui.homescreen.ui

// --- START OF ADDED IMPORTS ---
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
// --- END OF ADDED IMPORTS ---

// --- START OF ✅ ADDED IMPORTS ---
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material3.SnackbarDuration // ✅ ADDED
import androidx.compose.material3.SnackbarResult // ✅ ADDED
// --- END OF ✅ ADDED IMPORTS ---

import android.Manifest
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.domain.model.AutocompletePrediction
import com.example.invyucab_project.domain.model.HomeUiState
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

    // --- START OF ✅ MODIFIED CODE ---
    val apiError by viewModel.apiError // ✅ MODIFIED: Read the State directly
    val snackbarHostState = remember { SnackbarHostState() } // State for snackbar
    // --- END OF ✅ MODIFIED CODE ---

    // --- START OF ✅ ADDED LIFECYCLE OBSERVER ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // This event fires every time the screen becomes active
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-run the location check
                viewModel.getCurrentLocation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // This cleans up the observer when the composable is destroyed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // --- END OF ✅ ADDED LIFECYCLE OBSERVER ---

    // --- START OF PERMISSION LOGIC ---

    val context = LocalContext.current

    // Launcher for App PERMISSIONS
    val permissionSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // This block executes when returning from settings.
        // ✅ We can also trigger a re-check here
        viewModel.getCurrentLocation()
    }

    // --- START OF ✅ ADDED CODE ---
    // Launcher for Location SERVICES (GPS Toggle)
    val locationServiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // User has returned from the location settings screen.
        // Re-check if they turned it on.
        viewModel.getCurrentLocation()
    }
    // --- END OF ✅ ADDED CODE ---


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
            // ✅ Permission was just granted, fetch location
            viewModel.getCurrentLocation()
            // viewModel.onLocationPermissionGranted()
        }
    }

    val showPermissionBanner = !locationPermissionsState.allPermissionsGranted && permissionRequestLaunched

    // ✅ THIS IS THE KEY LOGIC
    val onAllowClick: () -> Unit = {
        if (locationPermissionsState.shouldShowRationale) {
            // Case 1: User denied once. Show request dialog again.
            // THIS IS THE "ALLOW -> APP PERMISSION" flow you want.
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            // Case 2: User permanently denied ("Don't ask again").
            // It is IMPOSSIBLE to show the dialog. We must send to settings.
            // THIS IS THE "ALLOW -> APP INFO" flow.
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            permissionSettingsLauncher.launch(intent)
        }
    }
    // --- END OF PERMISSION LOGIC ---


    // --- START OF ✅ MODIFIED CODE ---
    // This LaunchedEffect will react when apiError changes
    LaunchedEffect(apiError) {
        apiError?.let { message ->

            // Check if this is the specific GPS error
            if (message.contains("location services (GPS)")) {
                // Show snackbar with "Turn On" action
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Turn On",
                    duration = SnackbarDuration.Long // Keep it on screen longer
                )

                if (result == SnackbarResult.ActionPerformed) {
                    // User clicked "Turn On"
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationServiceLauncher.launch(intent)
                }

            } else {
                // Show a normal snackbar for all other errors
                snackbarHostState.showSnackbar(message)
            }

            // Clear the error in the ViewModel so it doesn't show again
            // until the next time we check (on resume)
            viewModel.clearApiError() // This function is from BaseViewModel
        }
    }
    // --- END OF ✅ MODIFIED CODE ---


    // Collect navigation events
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
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

    val isButtonEnabled =
        !uiState.pickupPlaceId.isNullOrBlank() && !uiState.dropPlaceId.isNullOrBlank()

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }, // <-- ✅ ADDED
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
                    item {
                        SearchButton(
                            text = "Set on map",
                            icon = Icons.Default.GpsFixed,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        AddressRow(
                            icon = Icons.Default.Star,
                            title = "Saved Places",
                            subtitle = "Manage your saved locations",
                            onClick = { /* TODO: Handle saved places */ }
                        )
                    }
                }
            }
        }
    }
}

// --- Other Composables (Unchanged) ---

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
            .background(CabVeryLightMint) // Match the Scaffold's top area
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
fun SearchButton(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { /* TODO */ },
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, LightSlateGray)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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

@Composable
private fun AddressRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
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
            imageVector = icon,
            contentDescription = title,
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
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}