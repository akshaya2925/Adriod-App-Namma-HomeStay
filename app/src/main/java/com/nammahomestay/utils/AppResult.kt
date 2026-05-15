package com.nammahomestay.utils

sealed class AppResult<out T> {
    object Loading : AppResult<Nothing>()
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()
}
