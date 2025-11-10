package com.example.invyucab_project.mainui.homescreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel // ✅ INHERITS FROM standard ViewModel
import com.example.invyucab_project.domain.model.ExploreItem
import com.example.invyucab_project.domain.model.PlaceItem
import com.example.invyucab_project.domain.model.RecentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() { // ✅ CHANGED

    // Dummy data for "Explore" section
    val exploreItems = listOf(
        ExploreItem(Icons.Default.Inventory2, "Parcel"),
        ExploreItem(Icons.Default.ElectricRickshaw, "Auto"),
        ExploreItem(Icons.Default.LocalTaxi, "Cab Economy"),
        ExploreItem(Icons.Default.TwoWheeler, "Bike")
    )

    // Dummy data for "Go Places" section
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