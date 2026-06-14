package com.example.morawallet.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.ChartLegend
import com.example.morawallet.core.ui.components.DonutChart
import com.example.morawallet.core.ui.components.DonutSlice
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LegendEntry
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraConfirmDialog
import com.example.morawallet.core.ui.components.TransactionRow
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    walletId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel = moraViewModel {
        WalletDetailViewModel(it.walletRepository, it.transactionRepository, it.authRepository, walletId)
    }
    val state = viewModel.state
    val snackbar = LocalSnackbarController.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            snackbar?.success("Wallet deleted")
            onDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.wallet?.name ?: "Wallet",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(walletId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingView(Modifier.padding(padding))
            state.wallet == null -> ErrorView(
                message = state.error ?: "Wallet not found",
                modifier = Modifier.padding(padding),
            )

            else -> {
                val wallet = state.wallet!!
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
                ) {
                    item { BalanceCard(wallet) }

                    val breakdown = state.transactions
                        .filter { it.typeEnum == TransactionType.EXPENSE }
                        .groupBy { it.category.ifBlank { "Other" } }
                        .map { (cat, list) -> cat to list.sumOf { it.amount } }
                        .filter { it.second > 0.0 }
                        .sortedByDescending { it.second }
                    if (breakdown.isNotEmpty()) {
                        item { WalletCategoryDonut(breakdown, wallet.currencyCode) }
                    }

                    item {
                        Text(
                            "Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs),
                        )
                    }
                    if (state.transactions.isEmpty()) {
                        item {
                            Text(
                                "No transactions for this wallet yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(state.transactions, key = { it.id }) { txn ->
                            TransactionRow(
                                transaction = txn,
                                walletsById = state.walletsById,
                                onClick = { onTransactionClick(txn.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        MoraConfirmDialog(
            title = "Delete wallet?",
            message = "This removes the wallet and all of its transactions. This cannot be undone.",
            confirmLabel = "Delete",
            icon = Icons.Filled.Delete,
            destructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun WalletCategoryDonut(breakdown: List<Pair<String, Double>>, currency: String) {
    val palette = MoraTheme.colors.chart
    val total = breakdown.sumOf { it.second }
    val top = breakdown.take(6)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Spending by category", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DonutChart(
                    slices = top.mapIndexed { i, s -> DonutSlice(s.second, palette[i % palette.size]) },
                    diameter = 120.dp,
                    centerLabel = "Spent",
                    centerValue = CurrencyFormatter.format(total, currency),
                )
                ChartLegend(
                    entries = top.mapIndexed { i, s ->
                        LegendEntry(
                            label = s.first,
                            color = palette[i % palette.size],
                            value = CurrencyFormatter.format(s.second, currency),
                            percent = (s.second / total * 100).toFloat(),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(wallet: Wallet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = CurrencyFormatter.format(wallet.balance, wallet.currencyCode),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = wallet.currencyCode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
