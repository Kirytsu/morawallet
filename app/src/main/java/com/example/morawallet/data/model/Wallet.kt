package com.example.morawallet.data.model

/** A currency account. Stored at users/{uid}/wallets/{id}. */
data class Wallet(
    val id: String = "",
    val name: String = "",
    val currencyCode: String = "USD",
    val balance: Double = 0.0,
    /** Index into the wallet color palette. */
    val colorIndex: Int = 0,
    val createdAt: Long = 0L,
)
