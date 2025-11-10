package com.example.invyucab_project.mainui.locationsearchscreen.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.model.EditingField
import com.example.invyucab_project.domain.model.SearchLocation
import com.example.invyucab_project.mainui.locationsearchscreen.viewmodel.LocationSearchViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    navController: NavController,
    viewModel: LocationSearchViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val pickupDescription = viewModel.pickupDescription
    val dropDescription = viewModel.dropDescription
    val activeField = viewModel.activeField
    val context = LocalContext.current

    val pickupFocusRequester = remember { FocusRequester() }
    val dropFocusRequester = remember { FocusRequester() }

    data class PendingNavigation(val pickupId: String?, val pickupDesc: String, val dropId: String?, val dropDesc: String) // Made dropId nullable
    var pendingLocationToNavigate by remember { mutableStateOf<PendingNavigation?>(null) }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                pendingLocationToNavigate?.let {
                    // We know dropId is not null here because we check before launching
                    navController.navigate(
                        Screen.RideSelectionScreen.createRoute(
                            pickupPlaceId = it.pickupId,
                            pickupDescription = it.pickupDesc,
                            dropPlaceId = it.dropId!!,
                            dropDescription = it.dropDesc
                        )
                    )
                    pendingLocationToNavigate = null // Clear
                }
            } else {
                Toast.makeText(context, "Location permission is required to find rides.", Toast.LENGTH_LONG).show()
                pendingLocationToNavigate = null // Clear
            }
        }
    )

    // Focus the correct text field when the activeField changes
    LaunchedEffect(activeField) {
        if (activeField == EditingField.PICKUP) {
            pickupFocusRequester.requestFocus()
        } else {
            dropFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Route", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO */ }) {
                        Text("For me", color = Color.Black)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White,
        // ✅✅✅ START OF FIX ✅✅✅
        // Add systemBarsPadding to the Scaffold to respect the status/navigation bars
        modifier = Modifier.systemBarsPadding()
        // ✅✅✅ END OF FIX ✅✅✅
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Apply padding from Scaffold
                .padding(horizontal = 16.dp) // Apply original horizontal padding
                // ✅✅✅ START OF FIX ✅✅✅
                // Add imePadding() here. This automatically adds padding to the
                // bottom of the Column when the keyboard is open.
                .imePadding()
            // ✅✅✅ END OF FIX ✅✅✅
        ) {
            SearchInputSection(
                pickupDescription = pickupDescription,
                dropDescription = dropDescription,
                activeField = activeField,
                onFieldActivated = viewModel::onFieldActivated,
                onFieldFocusLost = viewModel::onFieldFocusLost,
                onQueryChanged = viewModel::onQueryChanged,
                onClearField = viewModel::onClearField,
                pickupFocusRequester = pickupFocusRequester,
                dropFocusRequester = dropFocusRequester
            )

            if (searchResults.isNotEmpty()) {
                Divider(color = LightSlateGray, modifier = Modifier.padding(top = 16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(searchResults) { location ->
                        RecentSearchItem(
                            location = location,
                            onLocationClick = {
                                viewModel.onSearchResultClicked(location)
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                SearchButton(
                    text = "Select on map",
                    icon = Icons.Default.GpsFixed,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f)) // This Spacer pushes the button to the bottom
                Button(
                    onClick = {
                        val pickupId = viewModel.pickupPlaceId
                        val dropId = viewModel.dropPlaceId
                        val pickupDesc = viewModel.pickupDescription
                        val dropDesc = viewModel.dropDescription

                        if (dropId == null || dropDesc.isEmpty()) {
                            Toast.makeText(context, "Please select a drop location", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (pickupId == null || pickupDesc.isEmpty()) {
                            Toast.makeText(context, "Please select a pickup location", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // We've confirmed dropId is not null, so we can pass it
                        val pendingNav = PendingNavigation(pickupId, pickupDesc, dropId, dropDesc)

                        val hasCoarsePermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasFinePermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCoarsePermission || hasFinePermission) {
                            navController.navigate(
                                Screen.RideSelectionScreen.createRoute(
                                    pickupPlaceId = pickupId,
                                    pickupDescription = pickupDesc,
                                    dropPlaceId = dropId,
                                    dropDescription = dropDesc
                                )
                            )
                        } else {
                            pendingLocationToNavigate = pendingNav
                            permissionLauncher.launch(locationPermissions)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp) // Add padding to the button itself
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    enabled = viewModel.pickupPlaceId != null && viewModel.dropPlaceId != null
                ) {
                    Text("Confirm Locations", color = Color.White, fontSize = 16.sp)
                }
            }
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
                if (value.isNotEmpty() && isFocused) {
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
    pickupDescription: String,
    dropDescription: String,
    activeField: EditingField,
    onFieldActivated: (EditingField) -> Unit,
    onFieldFocusLost: (EditingField) -> Unit,
    onQueryChanged: (String) -> Unit,
    onClearField: (EditingField) -> Unit,
    pickupFocusRequester: FocusRequester,
    dropFocusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        LocationConnectorGraphic()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            StyledTextField(
                value = pickupDescription,
                onValueChange = onQueryChanged,
                placeholder = "Enter pickup location",
                focusRequester = pickupFocusRequester,
                onFocusChanged = { isFocused ->
                    if (isFocused) {
                        onFieldActivated(EditingField.PICKUP)
                    } else {
                        onFieldFocusLost(EditingField.PICKUP)
                    }
                },
                onClear = { onClearField(EditingField.PICKUP) },
                isFocused = activeField == EditingField.PICKUP
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = dropDescription,
                onValueChange = onQueryChanged,
                placeholder = "Enter drop location",
                focusRequester = dropFocusRequester,
                onFocusChanged = { isFocused ->
                    if (isFocused) {
                        onFieldActivated(EditingField.DROP)
                    } else {
                        onFieldFocusLost(EditingField.DROP)
                    }
                },
                onClear = { onClearField(EditingField.DROP) },
                isFocused = activeField == EditingField.DROP
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
fun RecentSearchItem(
    location: SearchLocation,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLocationClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = location.icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LightSlateGray)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.Black
            )
            Text(
                text = location.address,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = Color.Gray
            )
        }
    }
}