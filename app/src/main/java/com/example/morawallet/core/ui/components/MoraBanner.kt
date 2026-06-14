package com.example.morawallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.morawallet.ui.theme.MoraTheme

/**
 * Inline, tinted message block for forms and screens — replaces scattered bare red
 * `Text` error lines with a consistent icon + container treatment.
 */
@Composable
fun MoraBanner(
    message: String,
    type: MessageType,
    modifier: Modifier = Modifier,
) {
    val accent = when (type) {
        MessageType.SUCCESS -> MoraTheme.colors.income
        MessageType.ERROR -> MoraTheme.colors.expense
        MessageType.WARNING -> MaterialTheme.colorScheme.tertiary
        MessageType.INFO -> MaterialTheme.colorScheme.primary
    }
    val icon: ImageVector = when (type) {
        MessageType.SUCCESS -> Icons.Filled.CheckCircle
        MessageType.ERROR -> Icons.Filled.Error
        MessageType.WARNING -> Icons.Filled.WarningAmber
        MessageType.INFO -> Icons.Filled.Info
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Convenience overload for the common error case. */
@Composable
fun MoraErrorBanner(message: String, modifier: Modifier = Modifier) =
    MoraBanner(message = message, type = MessageType.ERROR, modifier = modifier)
