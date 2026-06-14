package com.example.morawallet.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.util.ReportRange

/** Interactive Week / Month / Year selector that drives the report charts. */
@Composable
fun RangeFilterChips(
    selected: ReportRange,
    onSelect: (ReportRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportRange.entries.forEach { range ->
            val isSelected = range == selected
            Button(
                onClick = { onSelect(range) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                }
                Text(range.label)
            }
        }
    }
}
