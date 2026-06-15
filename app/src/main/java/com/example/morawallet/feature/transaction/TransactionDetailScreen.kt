package com.example.morawallet.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.IconAvatar
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraConfirmDialog
import com.example.morawallet.core.util.Categories
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.core.util.DateUtils
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.LocalMoraColors
import com.example.morawallet.ui.theme.Spacing
import com.example.morawallet.ui.theme.paletteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    txnId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel = moraViewModel {
        TransactionDetailViewModel(
            it.transactionRepository,
            it.walletRepository,
            it.authRepository,
            txnId,
        )
    }
    val state = viewModel.state
    val snackbar = LocalSnackbarController.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            snackbar?.success("Record deleted")
            onDeleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(txnId) }) {
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
            state.transaction == null -> ErrorView(
                message = state.error ?: "Record not found",
                modifier = Modifier.padding(padding),
            )

            else -> {
                val txn = state.transaction!!
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    DetailHeader(txn)
                    HorizontalDivider()
                    DetailRow("Wallet", state.walletsById[txn.walletId]?.name ?: "—")
                    if (txn.typeEnum == TransactionType.TRANSFER) {
                        DetailRow("To wallet", state.walletsById[txn.toWalletId]?.name ?: "—")
                        if (txn.convertedAmount != null && txn.toCurrencyCode != null) {
                            DetailRow(
                                "Received",
                                CurrencyFormatter.format(txn.convertedAmount, txn.toCurrencyCode),
                            )
                        }
                        txn.fxRate?.let { DetailRow("Rate", "%.4f".format(it)) }
                    } else {
                        DetailRow("Category", txn.category)
                    }
                    DetailRow("Date", DateUtils.formatDate(txn.date))
                    DetailRow("Time", DateUtils.formatTime(txn.date))
                    if (txn.note.isNotBlank()) NoteRow(txn.note)
                }
            }
        }
    }

    if (showDeleteDialog) {
        MoraConfirmDialog(
            title = "Delete record?",
            message = "This reverses its effect on wallet balances and cannot be undone.",
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
private fun DetailHeader(txn: Transaction) {
    val semantic = LocalMoraColors.current
    val color = when (txn.typeEnum) {
        TransactionType.INCOME -> semantic.income
        TransactionType.EXPENSE -> semantic.expense
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when (txn.typeEnum) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> ""
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconAvatar(
            icon = Categories.icon(txn.category),
            color = paletteColor(Categories.colorIndex(txn.category)),
            sizeDp = 64,
        )
        Text(
            text = prefix + CurrencyFormatter.format(txn.amount, txn.currencyCode),
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = txn.category.ifBlank { txn.typeEnum.name },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun NoteRow(note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Note", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(note, style = MaterialTheme.typography.bodyLarge)
    }
}
