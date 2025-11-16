package com.example.invyucab_project.mainui.driverprofilescreen.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverProfileViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase
) : BaseViewModel() {

    // TODO: Load real driver data from a repository
    val driverName: String = "Ozzie Berkstresser"
    val profileImageUrl: String = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=100&h=100&fit=crop&crop=faces"

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUserUseCase.invoke()
            // Send navigation event to the UI
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}