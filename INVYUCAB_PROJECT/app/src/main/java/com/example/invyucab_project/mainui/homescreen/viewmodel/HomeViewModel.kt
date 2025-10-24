package com.example.invyucab_project.mainui.homescreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Data classes for the new UI
data class ExploreItem(val icon: ImageVector, val label: String)
data class PlaceItem(val icon: ImageVector, val label: String) // Using icon for placeholder
data class RecentLocation(val name: String, val address: String)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    // Dummy data for "Explore" section
    val exploreItems = listOf(
        ExploreItem(Icons.Default.Inventory2, "Parcel"),
        ExploreItem(Icons.Default.ElectricRickshaw, "Auto"),
        ExploreItem(Icons.Default.LocalTaxi, "Cab Economy"),
        ExploreItem(Icons.Default.TwoWheeler, "Bike")
    )

    // Dummy data for "Go Places" section
    // We use placeholder icons here. You can replace R.drawable.img_airport, etc.
    val placeItems = listOf(
        PlaceItem(Icons.Default.Flight, "Jay Prakash Narayan..."),
        PlaceItem(Icons.Default.Train, "Patna Junction"),
        PlaceItem(Icons.Default.HomeWork, "Patliputra")
    )

    // Dummy data for recent locations
    val recentLocations = listOf(
        RecentLocation("Dr.Kewal Sharan", "Road Number 10, Rajendra Nagar, Patna..."),
        RecentLocation("PRO FITNESS GYM", "Mahatma Gandhi Nagar, Chitragupta Nagar..."),
        RecentLocation("IGIMS", "Sheikhpura, Patna, Bihar, India")
    )
}