package com.example.invyucab_project.mainui.driverdocument.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.invyucab_project.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DocumentState(
    val title: String,
    val icon: ImageVector
)

@HiltViewModel
class DriverDocumentsViewModel @Inject constructor() : BaseViewModel() {

    private val _documents = MutableStateFlow<List<DocumentState>>(emptyList())
    val documents = _documents.asStateFlow()

    init {
        loadDocuments()
    }

    // In a real app, you would fetch this data from your repository/API
    private fun loadDocuments() {
        _documents.value = listOf(
            DocumentState(
                title = "Vehicle Preferences",
                icon = Icons.Default.Tune
            )
        )
    }
}