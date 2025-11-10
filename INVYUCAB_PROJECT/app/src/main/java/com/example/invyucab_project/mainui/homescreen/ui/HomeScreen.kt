package com.example.invyucab_project.mainui.homescreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // ✅ Import TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
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
    Scaffold(
        topBar = {
            // ✅ MODIFIED: Pass the navigation logic to the SearchAppBar
            SearchAppBar(
                onClick = {
                    navController.navigate(Screen.LocationSearchScreen.route)
                }
            )
        },
        bottomBar = { AppBottomNavigation(navController = navController) },
        containerColor = Color.White
    ) { padding ->
        // ✅ MODIFIED: Replaced LazyColumn with an empty Box
        // This keeps the background color but removes all dummy items.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CabVeryLightMint.copy(alpha = 0.3f))
        ) {
            // Content area is now empty
        }
    }
}

// ✅ MODIFIED: The function now accepts an onClick lambda
@Composable
fun SearchAppBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick), // ✅ MODIFIED: Use the lambda here
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Where are you going?",
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

// ✅
// ✅ The previous Composable functions for dummy items were here.
// ✅ They have been removed as requested to clean up the UI.
// ✅
// ✅ RecentLocationItem (Removed)
// ✅ ExploreSection (Removed)
// ✅ ExploreItem (Removed)
// ✅ GoPlacesSection (Removed)
// ✅ PlaceItem (Removed)
// ✅ BannerCard (Removed)
// ✅ SectionHeader (Removed)
// ✅


// ... (AppBottomNavigation is unchanged) ...
@Composable
fun AppBottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavItem("Ride", Icons.Filled.Home, Screen.HomeScreen),
        BottomNavItem("All Services", Icons.Filled.Apps, Screen.AllServicesScreen),
        BottomNavItem("Travel", Icons.Filled.FlightTakeoff, Screen.TravelScreen),
        BottomNavItem("Profile", Icons.Filled.Person, Screen.ProfileScreen)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = Modifier.navigationBarsPadding(), // ✅ THIS IS THE FIX
        containerColor = Color.White,
        contentColor = Color.Gray,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title, fontSize = 12.sp) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CabMintGreen,
                    selectedTextColor = CabMintGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = CabVeryLightMint
                ),
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination (HomeScreen) to avoid building a large
                            // back stack as users toggle between tabs
                            popUpTo(Screen.HomeScreen.route) {
                                saveState = true
                            }
                            // Avoid re-launching the same screen
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

// ... (BottomNavItem is unchanged) ...
data class BottomNavItem(val title: String, val icon: ImageVector, val screen: Screen)