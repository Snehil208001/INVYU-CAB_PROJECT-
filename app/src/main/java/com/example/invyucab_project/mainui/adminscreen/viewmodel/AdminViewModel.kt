package com.example.invyucab_project.mainui.adminscreen.viewmodel


import androidx.lifecycle.ViewModel
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor() : BaseViewModel() {

    // Placeholder function for Manage Drivers
    fun onManageDriversClicked() {
        // In the future, we will navigate to a Driver List screen here
        sendEvent(UiEvent.Navigate(Screen.ManageDriversScreen.route))
    }

    // Placeholder function for Manage Riders
    fun onManageRidersClicked() {
        // In the future, we will navigate to a Rider List screen here
        sendEvent(UiEvent.ShowSnackbar("Manage Riders feature coming soon!"))
    }

    // Placeholder function for View Rides
    fun onViewRidesClicked() {
        sendEvent(UiEvent.ShowSnackbar("View Rides feature coming soon!"))
    }

    fun onLogoutClicked() {
        // Navigate back to Auth Screen and clear backstack if needed
        sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
    }
}