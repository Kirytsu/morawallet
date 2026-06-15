package com.example.morawallet.feature.transaction

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.AmountField
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.IconAvatar
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraBanner
import com.example.morawallet.core.ui.components.MoraButton
import com.example.morawallet.core.ui.components.MoraErrorBanner
import com.example.morawallet.core.ui.components.MoraTextField
import com.example.morawallet.core.ui.components.MessageType
import com.example.morawallet.core.ui.components.WalletPicker
import com.example.morawallet.core.util.Categories
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing
import com.example.morawallet.ui.theme.paletteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    type: String?,
    txnId: String?,
    onBack: () -> Unit,
) {
    val viewModel = moraViewModel {
        TransactionFormViewModel(
            it.transactionRepository,
            it.walletRepository,
            it.authRepository,
            type,
            txnId,
        )
    }
    val state = viewModel.state
    val snackbar = LocalSnackbarController.current

    LaunchedEffect(state.success) {
        if (state.success) {
            snackbar?.success(if (state.isEdit) "Record updated" else "Record added")
            onBack()
        }
    }

    val sourceCurrency = state.wallets.firstOrNull { it.id == state.walletId }?.currencyCode ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit record" else "New record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loadingWallets && state.wallets.isEmpty() -> LoadingView(Modifier.padding(padding))
            state.wallets.isEmpty() -> EmptyView(
                title = "Create a wallet first",
                subtitle = "You need at least one wallet before adding records.",
                modifier = Modifier.padding(padding),
            )

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TypeSelector(state.type, viewModel::onTypeChange)

                    AmountField(
                        value = state.amount,
                        onValueChange = viewModel::onAmountChange,
                        currencyCode = sourceCurrency,
                        label = "Amount",
                        error = state.amountError,
                    )

                    WalletPicker(
                        selectedId = state.walletId,
                        wallets = state.wallets,
                        onSelect = viewModel::onWalletChange,
                        label = if (state.type == TransactionType.TRANSFER) "From wallet" else "Wallet",
                        error = state.walletError,
                    )

                    if (state.type == TransactionType.TRANSFER) {
                        WalletPicker(
                            selectedId = state.toWalletId,
                            wallets = state.wallets,
                            onSelect = viewModel::onToWalletChange,
                            label = "To wallet",
                            error = state.toWalletError,
                        )
                        if (state.crossCurrencyTransfer) {
                            AmountField(
                                value = state.receivedAmount,
                                onValueChange = viewModel::onReceivedChange,
                                currencyCode = state.destCurrency.orEmpty(),
                                label = "Amount received (${state.destCurrency})",
                                error = state.receivedError,
                            )
                        }
                    } else {
                        Text("Category", style = MaterialTheme.typography.labelLarge)
                        CategorySelector(
                            type = state.type,
                            selected = state.category,
                            onSelect = viewModel::onCategoryChange,
                        )
                        if (state.categoryError != null) {
                            MoraBanner(state.categoryError!!, MessageType.ERROR)
                        }
                    }

                    MoraTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        label = "Note (optional)",
                        placeholder = "e.g. Lunch with team",
                        singleLine = false,
                    )

                    if (state.error != null) {
                        MoraErrorBanner(state.error!!)
                    }

                    MoraButton(
                        text = if (state.isEdit) "Save changes" else "Add record",
                        onClick = viewModel::save,
                        loading = state.loading,
                        leadingIcon = Icons.Filled.Check,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
) {
    val order = listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        order.forEach { type ->
            val color = when (type) {
                TransactionType.INCOME -> MoraTheme.colors.income
                TransactionType.EXPENSE -> MoraTheme.colors.expense
                TransactionType.TRANSFER -> MoraTheme.colors.transfer
            }
            Button(
                onClick = { onSelect(type) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == type) color else color.copy(alpha = 0.12f),
                    contentColor = if (selected == type) Color.White else color,
                ),
            ) {
                Text(
                    type.name.lowercase().replaceFirstChar { it.uppercase() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CategorySelector(
    type: TransactionType,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        items(Categories.forType(type)) { category ->
            val isSelected = category.name == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onSelect(category.name) },
            ) {
                IconAvatar(
                    icon = category.icon,
                    color = paletteColor(category.colorIndex),
                    sizeDp = 52,
                    modifier = if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier
                    },
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
