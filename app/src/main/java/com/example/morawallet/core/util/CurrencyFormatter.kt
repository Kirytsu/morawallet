package com.example.morawallet.core.util

import kotlin.math.abs

/** Formats monetary amounts with explicit currency symbols (e.g. Rp, ₹, ฿). */
object CurrencyFormatter {

    /** Currencies conventionally shown without decimal places. */
    private val ZERO_DECIMAL = setOf("JPY", "KRW", "IDR", "HUF", "CLP", "VND")

    fun format(amount: Double, currencyCode: String): String {
        val decimals = if (currencyCode in ZERO_DECIMAL) 0 else 2
        val number = "%,.${decimals}f".format(amount)
        return "${Currencies.symbol(currencyCode)} $number"
    }

    /** Prefixes an explicit + / - sign (used for transaction amounts). */
    fun formatSigned(amount: Double, currencyCode: String): String {
        val sign = when {
            amount > 0 -> "+"
            amount < 0 -> "-"
            else -> ""
        }
        return sign + format(abs(amount), currencyCode)
    }

    fun symbol(currencyCode: String): String = Currencies.symbol(currencyCode)

    /** Grouped amount without any currency symbol, e.g. 10000.0 -> "10,000.00". */
    fun groupedNoSymbol(amount: Double, decimals: Int = 2): String =
        "%,.${decimals}f".format(amount)

    /** Short form with K/M/B/T suffix for compact display (e.g. in pills/chips). */
    fun formatCompact(amount: Double, currencyCode: String): String {
        val sym = Currencies.symbol(currencyCode)
        val abs = abs(amount)
        val (divided, suffix) = when {
            abs >= 1_000_000_000_000.0 -> abs / 1_000_000_000_000.0 to "T"
            abs >= 1_000_000_000.0     -> abs / 1_000_000_000.0     to "B"
            abs >= 1_000_000.0         -> abs / 1_000_000.0         to "M"
            abs >= 1_000.0             -> abs / 1_000.0             to "K"
            else                       -> abs                        to ""
        }
        val decimals = if (currencyCode in ZERO_DECIMAL) 0 else 2
        val formatted = if (suffix.isEmpty()) {
            "%,.${decimals}f".format(divided)
        } else {
            if (divided >= 100) "%.0f%s".format(divided, suffix)
            else if (divided >= 10) "%.1f%s".format(divided, suffix)
            else "%.2f%s".format(divided, suffix)
        }
        val sign = if (amount < 0) "-" else ""
        return "$sign$sym $formatted"
    }
}
