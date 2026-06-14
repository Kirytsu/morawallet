package com.example.morawallet.core.util

data class CurrencyInfo(val code: String, val displayName: String)

/** Currencies supported for wallets and exchange rates (ECB / Frankfurter set + common). */
object Currencies {
    val ALL: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "US Dollar"),
        CurrencyInfo("EUR", "Euro"),
        CurrencyInfo("GBP", "British Pound"),
        CurrencyInfo("JPY", "Japanese Yen"),
        CurrencyInfo("AUD", "Australian Dollar"),
        CurrencyInfo("CAD", "Canadian Dollar"),
        CurrencyInfo("CHF", "Swiss Franc"),
        CurrencyInfo("CNY", "Chinese Yuan"),
        CurrencyInfo("HKD", "Hong Kong Dollar"),
        CurrencyInfo("SGD", "Singapore Dollar"),
        CurrencyInfo("INR", "Indian Rupee"),
        CurrencyInfo("IDR", "Indonesian Rupiah"),
        CurrencyInfo("MYR", "Malaysian Ringgit"),
        CurrencyInfo("THB", "Thai Baht"),
        CurrencyInfo("KRW", "South Korean Won"),
        CurrencyInfo("NZD", "New Zealand Dollar"),
        CurrencyInfo("SEK", "Swedish Krona"),
        CurrencyInfo("NOK", "Norwegian Krone"),
        CurrencyInfo("DKK", "Danish Krone"),
        CurrencyInfo("PLN", "Polish Zloty"),
        CurrencyInfo("CZK", "Czech Koruna"),
        CurrencyInfo("HUF", "Hungarian Forint"),
        CurrencyInfo("ZAR", "South African Rand"),
        CurrencyInfo("BRL", "Brazilian Real"),
        CurrencyInfo("MXN", "Mexican Peso"),
        CurrencyInfo("PHP", "Philippine Peso"),
        CurrencyInfo("TRY", "Turkish Lira"),
        CurrencyInfo("AED", "UAE Dirham"),
        CurrencyInfo("SAR", "Saudi Riyal"),
    )

    val CODES: List<String> = ALL.map { it.code }

    /** Explicit, recognizable symbols per currency (Java's locale symbols often fall back to the code). */
    private val SYMBOLS: Map<String, String> = mapOf(
        "USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥", "AUD" to "A$",
        "CAD" to "C$", "CHF" to "Fr", "CNY" to "¥", "HKD" to "HK$", "SGD" to "S$",
        "INR" to "₹", "IDR" to "Rp", "MYR" to "RM", "THB" to "฿", "KRW" to "₩",
        "NZD" to "NZ$", "SEK" to "kr", "NOK" to "kr", "DKK" to "kr", "PLN" to "zł",
        "CZK" to "Kč", "HUF" to "Ft", "ZAR" to "R", "BRL" to "R$", "MXN" to "Mex$",
        "PHP" to "₱", "TRY" to "₺", "AED" to "د.إ", "SAR" to "﷼",
    )

    fun symbol(code: String): String = SYMBOLS[code] ?: code

    fun displayName(code: String): String =
        ALL.firstOrNull { it.code == code }?.displayName ?: code
}
