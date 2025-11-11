package com.example.invyucab_project.core.utils.navigationsbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.ui.theme.CabMintGreen

// Data class to represent a navigation item
private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// List of navigation items
private val navItems = listOf(
    NavItem("Home", Icons.Default.Home, Screen.HomeScreen.route),
    NavItem("Services", Icons.Default.Apps, Screen.AllServicesScreen.route),
    NavItem("Travel", Icons.Default.TravelExplore, Screen.TravelScreen.route),
    NavItem("Profile", Icons.Default.Person, Screen.ProfileScreen.route)
)

@Composable
fun AppBottomNavigation(navController: NavController, selectedItem: String) {
    NavigationBar(
        containerColor = Color.White
    ) {
        navItems.forEach { item ->
            val isSelected = item.label.equals(selectedItem, ignoreCase = true)
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Prevent navigating to the same screen
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CabMintGreen,
                    selectedTextColor = CabMintGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}