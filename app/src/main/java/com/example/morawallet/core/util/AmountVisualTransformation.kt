package com.example.morawallet.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Live thousands-separator formatting for money inputs: as the user types `10000`
 * the field shows `10,000`, while the underlying state stays digit-only ("10000").
 *
 * The raw value is expected to be already sanitized to `[0-9]*` plus an optional
 * single `.` and decimals (see [sanitizeAmountInput]). Cursor positions are mapped
 * through the inserted group separators so editing in the middle behaves correctly.
 */
class AmountVisualTransformation(
    private val groupSeparator: Char = ',',
    private val decimalSeparator: Char = '.',
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val dotIndex = original.indexOf(decimalSeparator)
        val intPart = if (dotIndex >= 0) original.substring(0, dotIndex) else original
        val rest = if (dotIndex >= 0) original.substring(dotIndex) else ""
        val length = intPart.length

        val builder = StringBuilder()
        for (k in 0 until length) {
            if (k > 0 && (length - k) % 3 == 0) builder.append(groupSeparator)
            builder.append(intPart[k])
        }
        val groupedInt = builder.toString()
        val transformed = groupedInt + rest
        val commasInInt = groupedInt.length - length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= length) return offset + commasInInt
                var commasBefore = 0
                for (k in 1..offset) {
                    if ((length - k) % 3 == 0) commasBefore++
                }
                return offset + commasBefore
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                val commas = transformed.substring(0, clamped).count { it == groupSeparator }
                return (clamped - commas).coerceIn(0, original.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), mapping)
    }
}

/**
 * Keeps only digits and at most one decimal point, capping the fractional part to
 * [maxDecimals]. Strips leading zeros (but keeps a single `0` before a decimal).
 */
fun sanitizeAmountInput(raw: String, maxDecimals: Int = 2): String {
    val filtered = buildString {
        var seenDot = false
        for (c in raw) {
            when {
                c.isDigit() -> append(c)
                (c == '.' || c == ',') && !seenDot && length > 0 -> {
                    // Treat a typed comma as the decimal point only if no dot yet;
                    // grouping commas are never part of the raw value.
                    if (c == '.') {
                        seenDot = true
                        append('.')
                    }
                }
            }
        }
    }
    val dotIndex = filtered.indexOf('.')
    if (dotIndex < 0) return filtered.trimStart('0').ifEmpty { if (filtered.isNotEmpty()) "0" else "" }
    val intPart = filtered.substring(0, dotIndex).trimStart('0').ifEmpty { "0" }
    val fracPart = filtered.substring(dotIndex + 1).take(maxDecimals)
    return "$intPart.$fracPart"
}
