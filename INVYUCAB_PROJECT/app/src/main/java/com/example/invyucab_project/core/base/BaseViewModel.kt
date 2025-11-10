package com.example.invyucab_project.core.base

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * A parent ViewModel class to handle common UI states like loading and errors.
 *
 * Child ViewModels can inherit from this to reduce boilerplate.
 *
 * It provides:
 * 1.  `isLoading`: A read-only State<Boolean> for the UI to observe.
 * 2.  `apiError`: A read-only State<String?> for the UI to observe.
 * 3.  `safeLaunch`: A protected function to run coroutines that automatically
 * manages the loading state and catches any unexpected exceptions.
 */
abstract class BaseViewModel : ViewModel() {

    // --- LOADING STATE ---

    // This is protected, so only this BaseViewModel and its children can change it.
    protected val _isLoading = mutableStateOf(false)

    // This is public and read-only (State), for the Composable to observe.
    val isLoading: State<Boolean> = _isLoading

    // --- ERROR STATE ---

    // Protected, so only this class and its children can change it.
    protected val _apiError = mutableStateOf<String?>(null)

    // Public and read-only, for the Composable to observe.
    val apiError: State<String?> = _apiError

    /**
     * A helper function to launch a coroutine with automatic loading and error handling.
     * Note: This wrapper is for `isLoading` and *unexpected crashes*.
     * Expected errors (like "user not found") should be handled manually
     * using the `Result` class from the UseCase.
     *
     * @param block The suspend function to execute (e.g., an API call).
     */
    protected fun safeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                // Set loading to true before the block executes
                _isLoading.value = true

                // Execute the main task
                block()

            } catch (e: Exception) {
                // Set the error message if the task fails
                _apiError.value = e.message ?: "An unknown error occurred"
                Log.e("BaseViewModel", "safeLaunch caught an exception", e)

            } finally {
                // Set loading to false after the block completes or fails
                _isLoading.value = false
            }
        }
    }

    /**
     * Public function for the UI to call to dismiss the error message.
     */
    fun clearApiError() {
        _apiError.value = null
    }
}