package com.example.morawallet.data.model

/** Latest rates for one base currency. */
data class RatesSnapshot(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)

/** A single point on a historical rate chart. */
data class RatePoint(
    val date: String,
    val value: Double,
)
