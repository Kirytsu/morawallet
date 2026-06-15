package com.example.morawallet.feature.markets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.util.Currencies
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@Composable
fun MarketsScreen(
    onOpenConverter: () -> Unit,
    onRateClick: (base: String, quote: String) -> Unit,
) {
    val viewModel = moraViewModel {
        MarketsViewModel(it.exchangeRateRepository, it.userRepository, it.authRepository)
    }
    val state = viewModel.state

    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader(
            baseCurrency = state.baseCurrency,
            date = state.date,
            onOpenConverter = onOpenConverter,
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            label = { Text("Search currency") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
        )

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::refresh)
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                itemsIndexed(state.visibleRates, key = { _, item -> item.first }) { index, (code, rate) ->
                    RateCard(
                        code = code,
                        rate = rate,
                        color = MoraTheme.colors.chart[index % MoraTheme.colors.chart.size],
                        onClick = { onRateClick(state.baseCurrency, code) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketHeader(
    baseCurrency: String,
    date: String,
    onOpenConverter: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Base currency", style = MaterialTheme.typography.labelMedium, color = Color.White)
                Text(
                    baseCurrency,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (date.isNotEmpty()) "Updated $date" else Currencies.displayName(baseCurrency),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FilledTonalButton(onClick = onOpenConverter) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Text("Convert", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun RateCard(
    code: String,
    rate: Double,
    color: Color,
    onClick: () -> Unit,
) {
    val content = if (color.luminance() > 0.45f) Color(0xFF102033) else Color.White
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(1.dp, content),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(content),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(Currencies.symbol(code), color = color, style = MaterialTheme.typography.labelLarge)
                }
                Column(modifier = Modifier.padding(start = Spacing.sm).weight(1f)) {
                    Text(code, style = MaterialTheme.typography.titleMedium, color = content)
                    Text(
                        Currencies.displayName(code),
                        style = MaterialTheme.typography.bodySmall,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                "%.4f".format(rate),
                style = MaterialTheme.typography.titleLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("Tap for history", style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}
