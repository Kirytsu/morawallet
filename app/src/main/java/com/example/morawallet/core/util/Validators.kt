package com.example.morawallet.core.util

import android.util.Patterns

/** Lightweight field validators returning a user-facing error, or null when valid. */
object Validators {

    const val MIN_PASSWORD_LENGTH = 6

    fun requiredError(value: String, field: String = "This field"): String? =
        if (value.isBlank()) "$field is required" else null

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun confirmPasswordError(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Confirm your password"
        password != confirm -> "Passwords do not match"
        else -> null
    }

    /** Parses a positive amount, or null if invalid / non-positive. */
    fun parsePositiveAmount(raw: String): Double? =
        raw.replace(",", "").trim().toDoubleOrNull()?.takeIf { it > 0.0 }
}
