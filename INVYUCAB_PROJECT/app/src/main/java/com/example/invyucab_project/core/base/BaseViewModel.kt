package com.example.invyucab_project.core.base

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * A parent ViewModel class to handle common UI states like loading and errors,
 * and to manage one-time UI events like navigation or snackbars.
 */
abstract class BaseViewModel : ViewModel() {

    // --- LOADING STATE ---
    protected val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // --- ERROR STATE ---
    protected val _apiError = mutableStateOf<String?>(null)
    val apiError: State<String?> = _apiError

    /**
     * This is for "one-time" events that should not be re-triggered
     * on configuration change, like navigation or showing a Snackbar.
     */
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    /**
     * Emits a new UI event.
     */
    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    /**
     * Public function for the UI to call to dismiss the error message.
     */
    fun clearApiError() {
        _apiError.value = null
    }

    /**
     * Sealed class representing all possible one-time UI events.
     */
    sealed class UiEvent {
        data class Navigate(val route: String) : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}