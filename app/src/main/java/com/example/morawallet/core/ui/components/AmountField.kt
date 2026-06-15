package com.example.morawallet.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.morawallet.core.util.AmountVisualTransformation
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.core.util.sanitizeAmountInput

private const val MAX_INT_DIGITS = 12

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
        onValueChange = { raw ->
            val sanitized = sanitizeAmountInput(raw)
            val dotIdx = sanitized.indexOf('.')
            val intLen = if (dotIdx >= 0) dotIdx else sanitized.length
            if (intLen <= MAX_INT_DIGITS) onValueChange(sanitized)
        },
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
        textStyle = MaterialTheme.typography.titleLarge,
        visualTransformation = AmountVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
