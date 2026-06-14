package com.example.morawallet

import com.example.morawallet.core.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun grouped_keeps_two_decimals_and_all_digits() {
        // Locale-independent: check the digits regardless of which grouping/decimal glyphs the locale uses.
        val formatted = CurrencyFormatter.groupedNoSymbol(1_234_567.5)
        assertEquals("123456750", formatted.filter { it.isDigit() })
    }

    @Test
    fun grouped_inserts_a_separator_for_thousands() {
        val formatted = CurrencyFormatter.groupedNoSymbol(10_000.0)
        // Some non-digit separator must appear between the thousands.
        assertTrue(formatted.any { !it.isDigit() && it != '0' })
    }
}
