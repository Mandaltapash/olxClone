package com.tods.project_olx.utils
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

// Extension functions for ViewModels
fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        action(data!!)
    }
    return this
}

fun <T> Resource<T>.onError(action: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        action(message!!)
    }
    return this
}

fun <T> Resource<T>.onLoading(action: () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        action()
    }
    return this
}

// Custom Exceptions
sealed class AppException : Exception() {
    data class NetworkException(override val message: String) : AppException()
    data class AuthException(override val message: String) : AppException()
    data class ValidationException(override val message: String) : AppException()
    data class StorageException(override val message: String) : AppException()
    data class UnknownException(override val message: String) : AppException()
}

// Extension to convert exceptions to user-friendly messages
fun Exception.toUserMessage(): String {
    return when (this) {
        is AppException.NetworkException -> "Network error: Please check your connection"
        is AppException.AuthException -> message
        is AppException.ValidationException -> message
        is AppException.StorageException -> "Failed to upload image"
        else -> "Something went wrong. Please try again"
    }
}

// Validator class
object Validator {
    fun validateEmail(email: String): ValidationResult {
        return if (email.isEmpty()) {
            ValidationResult.Error("Email cannot be empty")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationResult.Error("Invalid email format")
        } else {
            ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Password cannot be empty")
            password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters")
            else -> ValidationResult.Success
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        return when {
            phone.isEmpty() -> ValidationResult.Error("Phone number cannot be empty")
            cleanPhone.length < 10 -> ValidationResult.Error("Invalid phone number")
            else -> ValidationResult.Success
        }
    }

    fun validateAdTitle(title: String): ValidationResult {
        return when {
            title.isEmpty() -> ValidationResult.Error("Title cannot be empty")
            title.length < 3 -> ValidationResult.Error("Title must be at least 3 characters")
            title.length > 100 -> ValidationResult.Error("Title is too long")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

// Network checker
class NetworkChecker(private val context: Context) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.isConnected == true
        }
    }
}