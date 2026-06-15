package com.example.morawallet.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.ChartLegend
import com.example.morawallet.core.ui.components.DonutChart
import com.example.morawallet.core.ui.components.DonutSlice
import com.example.morawallet.core.ui.components.IncomeExpenseBarChart
import com.example.morawallet.core.ui.components.LegendEntry
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.RangeFilterChips
import com.example.morawallet.core.ui.components.TransactionRow
import com.example.morawallet.core.ui.components.WalletCard
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoneyTextStyle
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@Composable
fun DashboardScreen(
    onSeeAllWallets: () -> Unit,
    onWalletClick: (String) -> Unit,
    onAddWallet: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onSeeAllMarkets: () -> Unit,
    onAddTransaction: (String) -> Unit,
) {
    val viewModel = moraViewModel {
        DashboardViewModel(
            it.walletRepository,
            it.transactionRepository,
            it.userRepository,
            it.exchangeRateRepository,
            it.authRepository,
        )
    }
    val state = viewModel.state

    if (state.loading) {
        LoadingView()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            PortfolioCard(
                base = state.baseCurrency,
                value = state.portfolioValue,
                income = state.rangeIncome,
                expense = state.rangeExpense,
                walletCount = state.wallets.size,
                onIncomeClick = { onAddTransaction(TransactionType.INCOME.name) },
                onExpenseClick = { onAddTransaction(TransactionType.EXPENSE.name) },
            )
        }
        item { QuickAddRow(onAddTransaction) }

        item { SectionHeader("Wallets", Icons.Filled.AccountBalanceWallet, if (state.wallets.isEmpty()) null else onSeeAllWallets) }
        if (state.wallets.isEmpty()) {
            item { AddWalletCta(onAddWallet) }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    items(state.wallets, key = { it.id }) { wallet ->
                        WalletCard(
                            name = wallet.name,
                            currencyCode = wallet.currencyCode,
                            balance = wallet.balance,
                            colorIndex = wallet.colorIndex,
                            onClick = { onWalletClick(wallet.id) },
                            modifier = Modifier.width(250.dp),
                        )
                    }
                    item { AddWalletTile(onAddWallet) }
                }
            }
        }

        item { SectionHeader("Reports", Icons.Filled.BarChart) }
        item { RangeFilterChips(selected = state.range, onSelect = viewModel::setRange) }
        item { IncomeExpenseCard(state) }
        item { CategoryBreakdownCard(state, viewModel::setCategoryReportType) }

        item { SectionHeader("Recent transactions", Icons.AutoMirrored.Filled.ReceiptLong, onSeeAllTransactions) }
        if (state.recentTransactions.isEmpty()) {
            item { EmptyHint("No transactions yet.") }
        } else {
            items(state.recentTransactions.take(5), key = { it.id }) { txn ->
                TransactionRow(
                    transaction = txn,
                    walletsById = state.walletsById,
                    onClick = { onTransactionClick(txn.id) },
                )
            }
        }

        item { SectionHeader("Exchange rates", Icons.Filled.CurrencyExchange, onSeeAllMarkets) }
        val summaryRates = state.rates.entries.sortedBy { it.key }.take(4)
        if (summaryRates.isEmpty()) {
            item { EmptyHint("Rates unavailable.") }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
                ) {
                    Column(modifier = Modifier.padding(Spacing.sm)) {
                        summaryRates.forEach { (code, rate) ->
                            val symbol = com.example.morawallet.core.util.Currencies.symbol(code)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${state.baseCurrency} -> $code", style = MaterialTheme.typography.bodyLarge)
                                Text("$symbol ${"%,.2f".format(rate)}", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioCard(
    base: String,
    value: Double,
    income: Double,
    expense: Double,
    walletCount: Int,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val white = Color.White
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = white,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Total balance", style = MaterialTheme.typography.labelLarge, color = white)
            }
            Text(
                text = CurrencyFormatter.formatCompact(value, base),
                style = MoneyTextStyle,
                color = white,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Text(
                text = "Across $walletCount wallet${if (walletCount == 1) "" else "s"} - shown in $base",
                style = MaterialTheme.typography.bodySmall,
                color = white,
            )
            Spacer(Modifier.size(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                FlowPill(
                    icon = Icons.Filled.ArrowUpward,
                    label = "Income",
                    value = CurrencyFormatter.formatCompact(income, base),
                    tint = MoraTheme.colors.income,
                    onClick = onIncomeClick,
                    modifier = Modifier.weight(1f),
                )
                FlowPill(
                    icon = Icons.Filled.ArrowDownward,
                    label = "Expense",
                    value = CurrencyFormatter.formatCompact(expense, base),
                    tint = MoraTheme.colors.expense,
                    onClick = onExpenseClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FlowPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val white = Color.White
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(tint)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = white, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = white)
            Text(value, style = MaterialTheme.typography.titleSmall, color = white)
        }
    }
}

@Composable
private fun QuickAddRow(onAddTransaction: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
        QuickTile("Income", Icons.Filled.ArrowUpward, MoraTheme.colors.income, Modifier.weight(1f)) { onAddTransaction(TransactionType.INCOME.name) }
        QuickTile("Expense", Icons.Filled.ArrowDownward, MoraTheme.colors.expense, Modifier.weight(1f)) { onAddTransaction(TransactionType.EXPENSE.name) }
        QuickTile("Transfer", Icons.Filled.SwapHoriz, MoraTheme.colors.transfer, Modifier.weight(1f)) { onAddTransaction(TransactionType.TRANSFER.name) }
    }
}

@Composable
private fun QuickTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = tint),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(Spacing.xs))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun AddWalletCta(onAddWallet: () -> Unit) {
    Card(
        onClick = onAddWallet,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderStrong),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.padding(start = Spacing.md).weight(1f)) {
                Text("Add your first wallet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Create a wallet in any currency to start tracking your balance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddWalletTile(onAddWallet: () -> Unit) {
    Card(
        onClick = onAddWallet,
        modifier = Modifier
            .width(250.dp)
            .heightIn(min = 112.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.padding(start = Spacing.md)) {
                Text("Add wallet", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("Create another currency wallet", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun IncomeExpenseCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Income vs expense", style = MaterialTheme.typography.titleMedium)
            if (state.rangeIncome == 0.0 && state.rangeExpense == 0.0) {
                EmptyHint("No income or expense in this period.")
            } else {
                IncomeExpenseBarChart(
                    bars = state.incomeExpenseSeries,
                    incomeColor = MoraTheme.colors.income,
                    expenseColor = MoraTheme.colors.expense,
                )
                ChartLegend(
                    entries = listOf(
                        LegendEntry("Income", MoraTheme.colors.income, CurrencyFormatter.formatCompact(state.rangeIncome, state.baseCurrency)),
                        LegendEntry("Expense", MoraTheme.colors.expense, CurrencyFormatter.formatCompact(state.rangeExpense, state.baseCurrency)),
                    ),
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    state: DashboardUiState,
    onTypeChange: (TransactionType) -> Unit,
) {
    val palette = MoraTheme.colors.chart
    val slices = state.categoryBreakdown
    val total = slices.sumOf { it.amount }
    val isIncome = state.categoryReportType == TransactionType.INCOME
    val title = if (isIncome) "Income by category" else "Spending by category"
    val emptyText = if (isIncome) "No income in this period." else "No spending in this period."

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PieChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.sm))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                val incomeSelected = state.categoryReportType == TransactionType.INCOME
                val expenseSelected = state.categoryReportType == TransactionType.EXPENSE
                Button(
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (incomeSelected) MoraTheme.colors.income else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (incomeSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text("Income")
                }
                Button(
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expenseSelected) MoraTheme.colors.expense else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (expenseSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text("Expense")
                }
            }
            if (slices.isEmpty() || total <= 0.0) {
                EmptyHint(emptyText)
            } else {
                val top = slices.take(6)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    DonutChart(
                        slices = top.mapIndexed { i, s -> DonutSlice(s.amount, palette[i % palette.size]) },
                        diameter = 150.dp,
                        centerLabel = if (isIncome) "Earned" else "Spent",
                        centerValue = CurrencyFormatter.formatCompact(total, state.baseCurrency),
                    )
                    ChartLegend(
                        entries = top.mapIndexed { i, s ->
                            LegendEntry(
                                label = s.category,
                                color = palette[i % palette.size],
                                value = CurrencyFormatter.formatCompact(s.amount, state.baseCurrency),
                                percent = (s.amount / total * 100).toFloat(),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CategoryCards(
                        slices = top,
                        total = total,
                        palette = palette,
                        currency = state.baseCurrency,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCards(
    slices: List<com.example.morawallet.core.util.CategorySlice>,
    total: Double,
    palette: List<androidx.compose.ui.graphics.Color>,
    currency: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
        slices.forEachIndexed { index, slice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(palette[index % palette.size]),
                        )
                        Text(
                            slice.category,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Spacing.sm),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(CurrencyFormatter.formatCompact(slice.amount, currency), style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${(slice.amount / total * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.sm))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) { Text("See all") }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
    )
}
