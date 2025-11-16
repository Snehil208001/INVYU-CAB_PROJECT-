package com.example.invyucab_project.mainui.driverdocument.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.invyucab_project.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ✅ --- Data class updated: `status` field removed ---
data class DocumentState(
    val title: String,
    val icon: ImageVector
)
// ✅ --- End of change ---

@HiltViewModel
class DriverDocumentsViewModel @Inject constructor() : BaseViewModel() {

    private val _documents = MutableStateFlow<List<DocumentState>>(emptyList())
    val documents = _documents.asStateFlow()

    init {
        loadDocuments()
    }

    // In a real app, you would fetch this data from your repository/API
    private fun loadDocuments() {
        // ✅ --- List updated to remove `status` ---
        _documents.value = listOf(
            DocumentState(
                title = "Driver's License",
                icon = Icons.Default.Badge
            ),
            DocumentState(
                title = "Registration Certificate (RC)",
                icon = Icons.Default.Article
            ),
            DocumentState(
                title = "Vehicle Permit",
                icon = Icons.Default.Security
            ),
            DocumentState(
                title = "Vehicle Insurance",
                icon = Icons.Default.Shield
            ),
            DocumentState(
                title = "Proof of Address",
                icon = Icons.Default.Home
            ),
            DocumentState(
                title = "Identity Proof",
                icon = Icons.Default.AccountBox
            ),
            DocumentState(
                title = "Vehicle Preferences",
                icon = Icons.Default.Tune
            )
        )
        // ✅ --- End of change ---
    }
}