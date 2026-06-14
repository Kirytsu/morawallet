package com.example.morawallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morawallet.core.util.Currencies

/** A colored circle used for wallet and category markers. */
@Composable
fun CircleAvatar(
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun LetterAvatar(
    letter: String,
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
) {
    CircleAvatar(color = color, modifier = modifier, sizeDp = sizeDp) {
        Text(
            text = letter.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

@Composable
fun CurrencyAvatar(
    currencyCode: String,
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(color, CircleShape)
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = Currencies.symbol(currencyCode),
            color = Color.White,
            fontSize = (sizeDp * 0.36f).sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun IconAvatar(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
) {
    CircleAvatar(color = color, modifier = modifier, sizeDp = sizeDp) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((sizeDp / 2).dp),
        )
    }
}
