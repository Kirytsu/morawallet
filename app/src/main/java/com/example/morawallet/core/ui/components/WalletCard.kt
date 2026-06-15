package com.example.morawallet.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.util.Currencies
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.ui.theme.paletteColor

@Composable
fun WalletCard(
    name: String,
    currencyCode: String,
    balance: Double,
    colorIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = paletteColor(colorIndex)
    val content = if (background.luminance() > 0.45f) Color(0xFF111827) else Color.White

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, content),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WalletCurrencyMark(
                    currencyCode = currencyCode,
                    backgroundColor = background,
                    contentColor = content,
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        currencyCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = CurrencyFormatter.format(balance, currencyCode),
                style = MaterialTheme.typography.titleLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun WalletCurrencyMark(
    currencyCode: String,
    backgroundColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(contentColor)
            .border(1.dp, contentColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = Currencies.symbol(currencyCode),
            style = MaterialTheme.typography.titleMedium,
            color = backgroundColor,
        )
    }
}
