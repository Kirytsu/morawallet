package com.example.morawallet.core.util

/** One-shot result wrapper for async operations (network / Firestore calls). */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>
}

inline fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) block(data)
    return this
}

inline fun <T> Resource<T>.onError(block: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) block(message)
    return this
}
