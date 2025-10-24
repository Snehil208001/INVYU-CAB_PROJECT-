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
import com.example.invyucab_project.mainui.homescreen.viewmodel.ExploreItem
import com.example.invyucab_project.mainui.homescreen.viewmodel.HomeViewModel
import com.example.invyucab_project.mainui.homescreen.viewmodel.PlaceItem
import com.example.invyucab_project.mainui.homescreen.viewmodel.RecentLocation
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CabVeryLightMint.copy(alpha = 0.3f))
        ) {
            // ... (rest of LazyColumn is unchanged) ...
            // --- Recent Locations ---
            items(viewModel.recentLocations) { location ->
                RecentLocationItem(location = location)
            }

            item { Divider(color = Color.Gray.copy(alpha = 0.1f)) }

            // --- Explore Section ---
            item {
                ExploreSection(exploreItems = viewModel.exploreItems)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // --- Go Places Section ---
            item {
                GoPlacesSection(placeItems = viewModel.placeItems)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // --- Banner Card ---
            item {
                BannerCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
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

// ... (RecentLocationItem is unchanged) ...
@Composable
fun RecentLocationItem(location: RecentLocation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { /* TODO: Handle click */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Recent",
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
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = Color.Gray
        )
    }
}

// ... (ExploreSection is unchanged) ...
@Composable
fun ExploreSection(exploreItems: List<ExploreItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        SectionHeader(title = "Explore", onViewAll = { /* TODO */ })
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(exploreItems) { item ->
                ExploreItem(item = item)
            }
        }
    }
}


// ... (ExploreItem is unchanged) ...
@Composable
fun ExploreItem(item: ExploreItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp).clickable { /* TODO */ }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LightSlateGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(32.dp),
                tint = CabMintGreen
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ... (GoPlacesSection is unchanged) ...
@Composable
fun GoPlacesSection(placeItems: List<PlaceItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        SectionHeader(title = "Go Places with INVYU") // Changed from Rapido
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(placeItems) { item ->
                PlaceItem(item = item)
            }
        }
    }
}

// ... (PlaceItem is unchanged) ...
@Composable
fun PlaceItem(item: PlaceItem) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(LightSlateGray),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder Icon
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(40.dp),
                    tint = CabMintGreen
                )
                // TODO: Replace Box with Image(painter = painterResource(id = R.drawable.img_airport), ...)
            }
            Text(
                text = item.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ... (BannerCard is unchanged) ...
@Composable
fun BannerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CabVeryLightMint),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    "Pick up line of the day.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Parcel pick up\nin 5 minutes!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }
            // You need to add an image to your drawable folder for this
            // e.g., R.drawable.parcel_delivery_man
            /*
            Image(
                painter = painterResource(id = R.drawable.logo_auth), // Placeholder
                contentDescription = "Parcel Delivery",
                modifier = Modifier.fillMaxHeight().width(130.dp),
                contentScale = ContentScale.Crop
            )
            */
        }
    }
}


// ... (SectionHeader is unchanged) ...
@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onViewAll) {
            Text("View All", color = CabMintGreen, fontWeight = FontWeight.Bold)
        }
    }
}

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