package com.example.invyucab_project.core.common

import com.example.invyucab_project.R // You may need to add string resources

/**
 * A generic sealed class that wraps data with its loading status.
 * @param T The type of data being loaded.
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {

    /**
     * Represents a successful data fetch.
     * @param data The non-null data that was fetched.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents an error.
     * @param message A user-friendly error message.
     * @param data Optional data that might still be present (e.g., cached data).
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Represents the data loading state.
     * Data may be null or a previous (cached) value.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}