package com.example.morawallet.feature.markets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.ChartLegend
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.InteractiveLineChart
import com.example.morawallet.core.ui.components.LegendEntry
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateChartScreen(
    base: String,
    quote: String,
    onBack: () -> Unit,
) {
    val viewModel = moraViewModel {
        RateChartViewModel(it.exchangeRateRepository, base, quote)
    }
    val state = viewModel.state
    val ranges = listOf(7 to "1W", 30 to "1M", 90 to "3M", 365 to "1Y")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.displayBase} / ${state.displayQuote}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            val current = state.points.lastOrNull()?.value
            val first = state.points.firstOrNull()?.value

            RateSummaryCard(
                displayBase = state.displayBase,
                displayQuote = state.displayQuote,
                current = current,
                first = first,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ranges.forEach { (days, label) ->
                    val selected = state.days == days
                    Button(
                        onClick = { viewModel.setRange(days) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(label)
                    }
                }
            }

            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center,
                ) { LoadingView() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center,
                ) { ErrorView(state.error!!, onRetry = viewModel::load) }

                state.points.size < 2 -> Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center,
                ) { EmptyView(title = "Not enough data for this range") }

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
                    ) {
                        InteractiveLineChart(
                            values = state.points.map { it.value },
                            labels = state.points.map { it.date },
                            lineColor = MaterialTheme.colorScheme.primary,
                            valueFormat = { formatRate(it) },
                            modifier = Modifier.padding(Spacing.md),
                            height = 250.dp,
                        )
                    }
                    val high = state.points.maxOf { it.value }
                    val low = state.points.minOf { it.value }
                    ChartLegend(
                        entries = listOf(
                            LegendEntry("High", MoraTheme.colors.income, formatRate(high)),
                            LegendEntry("Low", MoraTheme.colors.expense, formatRate(low)),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RateSummaryCard(
    displayBase: String,
    displayQuote: String,
    current: Double?,
    first: Double?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text("1 $displayBase equals", style = MaterialTheme.typography.labelMedium, color = Color.White)
            Text(
                text = current?.let { "${formatRate(it)} $displayQuote" } ?: "-",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            if (current != null && first != null && first != 0.0) {
                val change = (current - first) / first * 100
                Text(
                    text = "%+.2f%% over period".format(change),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (change >= 0) Color(0xFFB9F6E6) else Color(0xFFFFD4DC),
                )
            }
        }
    }
}
