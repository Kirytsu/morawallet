package com.example.morawallet.data.model

/** User profile stored at users/{uid}. No-arg defaults required for Firestore. */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val baseCurrency: String = "USD",
    val createdAt: Long = 0L,
)
