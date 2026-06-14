package com.example.morawallet.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.morawallet.core.util.AmountVisualTransformation
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.core.util.sanitizeAmountInput

/**
 * Currency-aware money input. Shows the currency symbol as a prefix, formats the
 * typed number with live thousands separators ("10,000"), and keeps the raw value
 * digit-only in state. Uses a large display style so the amount reads like a figure,
 * not a form field.
 *
 * @param value raw digit string (e.g. "10000" or "1500.50"), never grouped.
 * @param onValueChange receives the sanitized raw value.
 */
@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    error: String? = null,
    enabled: Boolean = true,
) {
    val isError = error != null
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(sanitizeAmountInput(it)) },
        label = { Text(label) },
        prefix = { Text(CurrencyFormatter.symbol(currencyCode) + " ") },
        placeholder = { Text("0") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = if (isError) {
            { Text(error!!) }
        } else null,
        textStyle = LocalTextStyle.current.merge(MaterialTheme.typography.headlineSmall),
        visualTransformation = AmountVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
