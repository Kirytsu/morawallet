package com.example.morawallet.feature.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.DonutChart
import com.example.morawallet.core.ui.components.DonutSlice
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.TransactionRow
import com.example.morawallet.core.ui.components.showNativeDatePicker
import com.example.morawallet.core.util.Categories
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.core.util.DateUtils
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing
import com.example.morawallet.ui.theme.paletteColor

@Composable
fun TransactionsScreen(
    onTransactionClick: (String) -> Unit,
) {
    val viewModel = moraViewModel {
        TransactionListViewModel(it.transactionRepository, it.walletRepository, it.authRepository)
    }
    val state = viewModel.state

    when {
        state.loading -> LoadingView()
        state.error != null -> ErrorView(state.error!!)
        state.transactions.isEmpty() -> EmptyView(
            title = "No records yet",
            subtitle = "Add income, expense, or transfer records from Home.",
        )

        else -> {
            val visible = state.visibleTransactions
            val groups = visible.groupBy { DateUtils.dayKey(it.date) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item {
                    Text("Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Analyze records by type, period, wallet, and category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                item {
                    ReportControls(
                        state = state,
                        onQueryChange = viewModel::setQuery,
                        onTypeChange = viewModel::setRecordType,
                        onStartDate = viewModel::setStartDate,
                        onEndDate = viewModel::setEndDate,
                        onClearDates = viewModel::clearDates,
                        onWalletChange = viewModel::setWalletFilter,
                    )
                }

                item { ReportChartCard(state) }

                item {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    CategoryGrid(
                        state = state,
                        onSelectCategory = viewModel::setSelectedCategory,
                    )
                }

                item { RecordsHeader(state = state, count = visible.size) }

                if (visible.isEmpty()) {
                    item {
                        EmptyView(
                            title = "No matching records",
                            subtitle = "Try a different wallet, period, category, or search.",
                        )
                    }
                }

                groups.forEach { (_, txns) ->
                    item {
                        Text(
                            text = DateUtils.dayHeader(txns.first().date),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                    items(txns, key = { it.id }) { txn ->
                        TransactionRow(
                            transaction = txn,
                            walletsById = state.walletsById,
                            onClick = { onTransactionClick(txn.id) },
                            modifier = Modifier.padding(horizontal = 0.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportControls(
    state: TransactionsUiState,
    onQueryChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onStartDate: (Long?) -> Unit,
    onEndDate: (Long?) -> Unit,
    onClearDates: () -> Unit,
    onWalletChange: (String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text("Search records") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
        ) {
            TypeChip(state.recordType == TransactionType.INCOME, "Income", Icons.Filled.ArrowUpward, MoraTheme.colors.income) {
                onTypeChange(TransactionType.INCOME)
            }
            TypeChip(state.recordType == TransactionType.EXPENSE, "Expense", Icons.Filled.ArrowDownward, MoraTheme.colors.expense) {
                onTypeChange(TransactionType.EXPENSE)
            }
            TypeChip(state.recordType == TransactionType.TRANSFER, "Transfer", Icons.Filled.SwapHoriz, MoraTheme.colors.transfer) {
                onTypeChange(TransactionType.TRANSFER)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateButton("Start", state.startDate, onStartDate, Modifier.weight(1f))
            DateButton("End", state.endDate, onEndDate, Modifier.weight(1f))
        }
        if (state.startDate != null || state.endDate != null) {
            Button(
                onClick = onClearDates,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Clear dates")
            }
        }

        WalletFilterButton(
            selectedWallet = state.walletFilter?.let { state.walletsById[it] },
            wallets = state.wallets,
            onSelect = onWalletChange,
        )
    }
}

@Composable
private fun TypeChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.12f),
            contentColor = if (selected) Color.White else color,
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.padding(start = 6.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateButton(
    label: String,
    millis: Long?,
    onPick: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Button(
        onClick = {
            showNativeDatePicker(
                context = context,
                initialMillis = millis ?: System.currentTimeMillis(),
                onSelected = { onPick(it) },
            )
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (millis != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (millis != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            millis?.let(DateUtils::formatDate) ?: label,
            modifier = Modifier.padding(start = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WalletFilterButton(
    selectedWallet: Wallet?,
    wallets: List<Wallet>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedWallet != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (selectedWallet != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                selectedWallet?.let { "${it.name} (${it.currencyCode})" } ?: "All wallets",
                modifier = Modifier
                    .padding(horizontal = Spacing.sm)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp),
        ) {
            DropdownMenuItem(
                text = { WalletChoiceText("All wallets", null) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            wallets.forEach { wallet ->
                DropdownMenuItem(
                    text = { WalletChoiceText(wallet.name, wallet.currencyCode) },
                    onClick = {
                        onSelect(wallet.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun WalletChoiceText(
    name: String,
    code: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            code ?: "ALL",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(58.dp),
        )
        Text(
            name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReportChartCard(state: TransactionsUiState) {
    val breakdown = state.categoryBreakdown
    val total = breakdown.sumOf { it.second }
    val typeText = typeLabel(state.recordType)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                "$typeText by category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.recordType == TransactionType.TRANSFER) {
                Text(
                    "Transfers are listed below without category totals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                return@Column
            }
            if (breakdown.isEmpty() || total <= 0.0) {
                Text(
                    "No $typeText records in this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            } else {
                DonutChart(
                    slices = breakdown.take(8).map { item ->
                        DonutSlice(item.second, paletteColor(Categories.colorIndex(item.first)))
                    },
                    diameter = 190.dp,
                    centerLabel = "Total",
                    centerValue = money(total, state.activeCurrency),
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    state: TransactionsUiState,
    onSelectCategory: (String?) -> Unit,
) {
    if (state.recordType == TransactionType.TRANSFER) {
        return
    }
    val breakdown = state.categoryBreakdown
    val total = breakdown.sumOf { it.second }
    if (breakdown.isEmpty() || total <= 0.0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        ) {
            Text(
                "No category data for this filter.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.md),
            )
        }
        return
    }

    val allItems = breakdown.map { it.first to it.second }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        allItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                rowItems.forEach { (category, amount) ->
                    val color = category?.let { paletteColor(Categories.colorIndex(it)) } ?: typeAccent(state.recordType)
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        label = category ?: "All ${typeLabel(state.recordType).lowercase()}",
                        amount = amount,
                        percent = (amount / total).toFloat(),
                        color = color,
                        selected = state.selectedCategory == category,
                        currency = state.activeCurrency,
                        icon = category?.let { Categories.icon(it) }
                            ?: if (state.recordType == TransactionType.INCOME) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        onClick = { onSelectCategory(category) },
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    percent: Float,
    color: Color,
    selected: Boolean,
    currency: String?,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val background = if (selected) color else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected && background.luminance() <= 0.45f) Color.White else Color(0xFF102033)
    val iconContent = if (color.luminance() > 0.45f) Color(0xFF102033) else Color.White
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, if (selected) color else MoraTheme.colors.borderStrong),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (selected) content else color),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = if (selected) color else iconContent, modifier = Modifier.size(17.dp))
                }
                Text(
                    "${(percent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                money(amount, currency),
                style = MaterialTheme.typography.titleSmall,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecordsHeader(
    state: TransactionsUiState,
    count: Int,
) {
    val categoryText = state.selectedCategory?.let { " - $it" }.orEmpty()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${typeLabel(state.recordType)} records$categoryText",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            "$count",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .padding(start = Spacing.sm)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private fun money(amount: Double, currency: String?): String =
    if (currency != null) CurrencyFormatter.format(amount, currency) else CurrencyFormatter.groupedNoSymbol(amount)

private fun typeLabel(type: TransactionType): String =
    type.name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun typeAccent(type: TransactionType): Color = when (type) {
    TransactionType.INCOME -> MoraTheme.colors.income
    TransactionType.EXPENSE -> MoraTheme.colors.expense
    TransactionType.TRANSFER -> MoraTheme.colors.transfer
}
