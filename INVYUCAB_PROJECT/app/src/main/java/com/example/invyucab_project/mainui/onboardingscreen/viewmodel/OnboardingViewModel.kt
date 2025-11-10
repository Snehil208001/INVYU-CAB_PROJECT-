package com.example.invyucab_project.mainui.onboardingscreen.viewmodel

import androidx.lifecycle.ViewModel // ✅ INHERITS FROM standard ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.domain.usecase.SaveOnboardingUseCase // ✅ IMPORTED USECASE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveOnboardingUseCase: SaveOnboardingUseCase // ✅ INJECTED USECASE
) : ViewModel() { // ✅ CHANGED

    fun onGetStartedClicked() {
        viewModelScope.launch {
            saveOnboardingUseCase.invoke() // ✅ CALLS USECASE
        }
    }
}