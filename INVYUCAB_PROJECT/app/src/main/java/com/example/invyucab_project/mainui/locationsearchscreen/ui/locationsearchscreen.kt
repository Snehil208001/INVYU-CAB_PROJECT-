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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.locationsearchscreen.viewmodel.EditingField
import com.example.invyucab_project.mainui.locationsearchscreen.viewmodel.LocationSearchViewModel
import com.example.invyucab_project.mainui.locationsearchscreen.viewmodel.SearchLocation
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    navController: NavController,
    viewModel: LocationSearchViewModel = hiltViewModel()
) {
    // ✅ MODIFIED: Get all new state from ViewModel
    val focusRequester = remember { FocusRequester() }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery = viewModel.searchQuery
    val pickupDescription = viewModel.pickupDescription
    val dropDescription = viewModel.dropDescription
    val activeField = viewModel.activeField
    val context = LocalContext.current

    // State to hold the location we want to navigate to *after* permission is granted
    // ✅ MODIFIED: This now holds both pickup and drop info
    data class PendingNavigation(val pickupId: String?, val pickupDesc: String, val dropId: String, val dropDesc: String)
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
                // Permission was granted!
                pendingLocationToNavigate?.let {
                    navController.navigate(
                        Screen.RideSelectionScreen.createRoute(
                            pickupPlaceId = it.pickupId,
                            pickupDescription = it.pickupDesc,
                            dropPlaceId = it.dropId,
                            dropDescription = it.dropDesc
                        )
                    )
                    pendingLocationToNavigate = null // Clear
                }
            } else {
                // Permission was denied
                Toast.makeText(context, "Location permission is required to find rides.", Toast.LENGTH_LONG).show()
                pendingLocationToNavigate = null // Clear
            }
        }
    )

    // ✅ MODIFIED: Focus the search box when the activeField changes
    LaunchedEffect(activeField) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // ✅ MODIFIED: Title
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
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ✅ MODIFIED: This section now shows both fields
            SearchInputSection(
                pickupDescription = pickupDescription,
                dropDescription = dropDescription,
                activeField = activeField,
                onFieldActivated = viewModel::onFieldActivated
            )

            // ✅ ADDED: This is the single, real search box
            BasicTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp)
                    .focusRequester(focusRequester)
                    .background(LightSlateGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                singleLine = true,
                // ✅ ENHANCED: Added text style and cursor brush
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(CabMintGreen),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (activeField == EditingField.PICKUP) "Enter pickup location" else "Enter drop location",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clickable { viewModel.onSearchQueryChange("") }
                            )
                        }
                    }
                }
            )

            // ✅ MODIFIED: This logic is now simpler
            if (searchResults.isNotEmpty()) {
                Divider(color = LightSlateGray, modifier = Modifier.padding(top = 16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(searchResults) { location ->
                        RecentSearchItem(
                            location = location,
                            onLocationClick = {
                                // This click now just updates the ViewModel state
                                viewModel.onSearchResultClicked(location)
                            }
                        )
                    }
                }
            } else {
                // ✅ MODIFIED: Show "Set on map" and "Confirm"
                Spacer(modifier = Modifier.height(16.dp))
                SearchButton(
                    text = "Select on map",
                    icon = Icons.Default.GpsFixed,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        // ✅ MODIFIED: Navigation logic
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

                        val pendingNav = PendingNavigation(pickupId, pickupDesc, dropId, dropDesc)

                        // Check if permission is already granted
                        val hasCoarsePermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasFinePermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCoarsePermission || hasFinePermission) {
                            // Permission granted, navigate immediately
                            navController.navigate(
                                Screen.RideSelectionScreen.createRoute(
                                    pickupPlaceId = pickupId,
                                    pickupDescription = pickupDesc,
                                    dropPlaceId = dropId,
                                    dropDescription = dropDesc
                                )
                            )
                        } else {
                            // Permission NOT granted, set pending and launch
                            pendingLocationToNavigate = pendingNav
                            permissionLauncher.launch(locationPermissions)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    // Enable only if both fields are set
                    enabled = viewModel.pickupPlaceId != null && viewModel.dropPlaceId != null
                ) {
                    Text("Confirm Locations", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SearchInputSection(
    pickupDescription: String,
    dropDescription: String,
    activeField: EditingField,
    onFieldActivated: (EditingField) -> Unit
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
            // ✅ MODIFIED: Pickup Field
            val pickupIsActive = activeField == EditingField.PICKUP
            val pickupColor = if (pickupIsActive) CabMintGreen else Color.Gray.copy(alpha = 0.5f)
            val pickupTextColor = if (pickupDescription == "Your Current Location") CabMintGreen else Color.Black

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, pickupColor, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)) // ✅ Changed background
                    .clickable { onFieldActivated(EditingField.PICKUP) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = pickupDescription.ifEmpty { "Enter pickup location" },
                    color = if (pickupDescription.isEmpty()) Color.Gray else pickupTextColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ MODIFIED: Drop Field
            val dropIsActive = activeField == EditingField.DROP
            val dropColor = if (dropIsActive) CabMintGreen else Color.Gray.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, dropColor, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)) // ✅ Changed background
                    .clickable { onFieldActivated(EditingField.DROP) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = dropDescription.ifEmpty { "Enter drop location" },
                    color = if (dropDescription.isEmpty()) Color.Gray else Color.Black,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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