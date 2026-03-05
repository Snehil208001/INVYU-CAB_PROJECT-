package com.example.invyucab_project.mainui.profilescreen.paymentmethodscreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PaymentMethod(
    val id: String,
    val name: String,
    val details: String,
    val icon: ImageVector,
    val isDefault: Boolean = false
)

data class PaymentUiState(
    val defaultMethod: PaymentMethod? = null,
    val otherMethods: List<PaymentMethod> = emptyList()
)

@HiltViewModel
class PaymentMethodViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    // Holds the raw list of methods
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())

    init {
        // In a real app, you'd fetch this from a repository
        val initialMethods = listOf(
            PaymentMethod(
                id = "1",
                name = "Cash",
                details = "Default Payment Method",
                icon = Icons.Default.Money,
                isDefault = true
            ),
            PaymentMethod(
                id = "2",
                name = "UPI",
                details = "snehil@okhdfcbank",
                icon = Icons.Default.AccountBalance,
                isDefault = false
            ),
            PaymentMethod(
                id = "3",
                name = "Credit Card",
                details = "**** **** **** 5967",
                icon = Icons.Default.CreditCard,
                isDefault = false
            )
        )
        _paymentMethods.value = initialMethods
        processMethods(initialMethods)
    }

    private fun processMethods(methods: List<PaymentMethod>) {
        _uiState.update {
            it.copy(
                defaultMethod = methods.find { it.isDefault },
                otherMethods = methods.filter { !it.isDefault }
            )
        }
    }

    fun setDefault(methodId: String) {
        val updatedMethods = _paymentMethods.value.map {
            it.copy(isDefault = it.id == methodId)
        }
        _paymentMethods.value = updatedMethods
        processMethods(updatedMethods)
    }
}