package com.example.invyucab_project.mainui.locationsearchscreen.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
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
    val focusRequester = remember { FocusRequester() }
    val searchResults by viewModel.searchResults.collectAsState()
    val context = LocalContext.current

    // ✅ START: Corrected Permission Handling Logic

    // State to hold the location we want to navigate to *after* permission is granted
    var pendingLocationToNavigate by remember { mutableStateOf<SearchLocation?>(null) }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // This launcher's 'onResult' block will run AFTER the user responds to the permission dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                // Permission was granted!
                // Now, check if we have a location waiting and navigate to it.
                pendingLocationToNavigate?.let { location ->
                    navController.navigate(
                        Screen.RideSelectionScreen.createRoute(
                            placeId = location.placeId,
                            description = location.address
                        )
                    )
                    pendingLocationToNavigate = null // Clear the pending location
                }
            } else {
                // Permission was denied
                Toast.makeText(context, "Location permission is required to find rides.", Toast.LENGTH_LONG).show()
                pendingLocationToNavigate = null // Clear the pending location
            }
        }
    )
    // ✅ END: Corrected Permission Handling Logic

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drop", fontWeight = FontWeight.Bold) },
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
            SearchInputSection(
                searchQuery = viewModel.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                focusRequester = focusRequester
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchButton(
                    text = "Select on map",
                    icon = Icons.Default.GpsFixed,
                    modifier = Modifier.weight(1f)
                )
                SearchButton(
                    text = "Add stops",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = LightSlateGray)

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(searchResults) { location ->
                    RecentSearchItem(
                        location = location,
                        onLocationClick = {
                            // ✅ START: Updated Click Logic
                            // Check if permission is already granted
                            val hasCoarsePermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            val hasFinePermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCoarsePermission || hasFinePermission) {
                                // Permission is already granted, navigate immediately
                                navController.navigate(
                                    Screen.RideSelectionScreen.createRoute(
                                        placeId = location.placeId,
                                        description = location.address
                                    )
                                )
                            } else {
                                // Permission is NOT granted
                                // 1. Set the pending location
                                pendingLocationToNavigate = location
                                // 2. Launch the permission request. The 'onResult' block will handle navigation.
                                permissionLauncher.launch(locationPermissions)
                            }
                            // ✅ END: Updated Click Logic
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchInputSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(LightSlateGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Your Current Location", color = Color.Black, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .focusRequester(focusRequester)
                    .background(LightSlateGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                        if (searchQuery.isEmpty()) {
                            Text("Drop location", color = Color.Gray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                }
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