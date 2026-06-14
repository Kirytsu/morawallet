package com.example.morawallet

import androidx.compose.ui.text.AnnotatedString
import com.example.morawallet.core.util.AmountVisualTransformation
import com.example.morawallet.core.util.sanitizeAmountInput
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    private val transformation = AmountVisualTransformation()

    private fun grouped(raw: String): String =
        transformation.filter(AnnotatedString(raw)).text.text

    @Test
    fun groups_thousands() {
        assertEquals("10,000", grouped("10000"))
        assertEquals("1,234,567", grouped("1234567"))
        assertEquals("100", grouped("100"))
    }

    @Test
    fun keeps_decimal_part_ungrouped() {
        assertEquals("1,500.50", grouped("1500.50"))
        assertEquals("0.99", grouped("0.99"))
    }

    @Test
    fun offset_mapping_round_trips_at_end() {
        val transformed = transformation.filter(AnnotatedString("1234567"))
        val mapping = transformed.offsetMapping
        // "1234567" -> "1,234,567" (two separators), cursor at end (7) -> 9.
        assertEquals(9, mapping.originalToTransformed(7))
        assertEquals(7, mapping.transformedToOriginal(9))
    }

    @Test
    fun sanitize_strips_non_numeric_and_extra_dots() {
        assertEquals("10000", sanitizeAmountInput("10,000"))
        assertEquals("1500.50", sanitizeAmountInput("1500.50"))
        assertEquals("12.34", sanitizeAmountInput("12.3456"))
        assertEquals("0.55", sanitizeAmountInput("0.5.5"))
        assertEquals("", sanitizeAmountInput("abc"))
    }

    @Test
    fun sanitize_trims_leading_zeros() {
        assertEquals("100", sanitizeAmountInput("000100"))
        assertEquals("0.50", sanitizeAmountInput("00.50"))
    }
}
