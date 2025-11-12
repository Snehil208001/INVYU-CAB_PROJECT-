package com.example.invyucab_project.mainui.homescreen.ui

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
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Collect navigation events
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                else -> {}
            }
        }
    }

    val isButtonEnabled =
        !uiState.pickupPlaceId.isNullOrBlank() && !uiState.dropPlaceId.isNullOrBlank()

    Scaffold(
        containerColor = Color.White,
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
                    .background(Color.White) // Ensure background is white
                    .imePadding() // This pushes the whole Column up when keyboard appears
            ) {
                AnimatedVisibility(
                    visible = isButtonEnabled,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    // The Button is now part of the bottomBar
                    Button(
                        onClick = { viewModel.onContinueClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 16.dp) // Padding for the button
                            .padding(bottom = 8.dp), // Space between button and nav bar
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
                // The AppBottomNavigation is now below the button
                AppBottomNavigation(navController = navController, selectedItem = "Home")
            }
        }
    ) { padding -> // These paddingValues from the Scaffold now perfectly account for the nav bar AND the button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // ✅ This padding is all that's needed
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        ) {
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

            // Show results based on which field is active
            val results = if (uiState.activeField == SearchField.PICKUP) {
                uiState.pickupResults
            } else {
                uiState.dropResults
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // White background for the list
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                // ✅ No more fixed bottom padding needed
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
                    // Default items when not searching
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

// --- StyledTextField (This is correct) ---

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

// --- SearchInputSection (This is correct) ---

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

    // ✅ ADDED: Flag to skip first composition
    val isInitialRun = remember { mutableStateOf(true) }

    // This effect is necessary to handle focus changes
    // when a prediction is tapped
    LaunchedEffect(activeField) {
        // ✅ START OF MODIFIED CODE: KEYBOARD FIX
        if (isInitialRun.value) {
            // Skip the first effect run to prevent keyboard on launch
            // The default field is DROP, so this will trigger, set the flag, and not request focus
            isInitialRun.value = false
        } else {
            // This is a subsequent run, triggered by user interaction (e.g., tapping PICKUP)
            if (activeField == SearchField.PICKUP) {
                pickupFocusRequester.requestFocus()
            } else if (activeField == SearchField.DROP) {
                // This will run if the state changes *back* to drop
                dropFocusRequester.requestFocus()
            }
        }
        // ✅ END OF MODIFIED CODE
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

// --- Other Composables (Unchanged) ---

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