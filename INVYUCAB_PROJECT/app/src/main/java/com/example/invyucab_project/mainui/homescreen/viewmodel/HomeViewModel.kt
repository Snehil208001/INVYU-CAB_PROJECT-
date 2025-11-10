package com.example.invyucab_project.mainui.homescreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector // Keep this import for Icons
import androidx.lifecycle.ViewModel
// Refactored: UI state classes imported from domain.model
import com.example.invyucab_project.domain.model.ExploreItem
import com.example.invyucab_project.domain.model.PlaceItem
import com.example.invyucab_project.domain.model.RecentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Note: ExploreItem, PlaceItem, and RecentLocation were moved to
// domain/model/HomeUiState.kt


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