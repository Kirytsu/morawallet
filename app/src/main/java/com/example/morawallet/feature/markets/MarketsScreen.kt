package com.example.morawallet.feature.markets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            onBaseCurrencyChange = viewModel::setBaseCurrency,
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
    onBaseCurrencyChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

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
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.clickable { expanded = true },
                ) {
                    Text("Base currency", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            baseCurrency,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        if (date.isNotEmpty()) "Updated $date" else Currencies.displayName(baseCurrency),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 400.dp),
                ) {
                    Currencies.ALL.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${currency.code}  ${currency.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            onClick = {
                                onBaseCurrencyChange(currency.code)
                                expanded = false
                            },
                        )
                    }
                }
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp),
        colors = CardDefaults.cardColors(containerColor = color),
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
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(Currencies.symbol(code), color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                Column(modifier = Modifier.padding(start = Spacing.sm).weight(1f)) {
                    Text(code, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        Currencies.displayName(code),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                formatRate(rate),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("Tap for history", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

internal fun formatRate(rate: Double): String = when {
    rate >= 10_000 -> "%,.0f".format(rate)
    rate >= 100 -> "%.1f".format(rate)
    rate >= 1 -> "%.4f".format(rate)
    rate >= 0.01 -> "%.4f".format(rate)
    else -> "%.6f".format(rate)
}
