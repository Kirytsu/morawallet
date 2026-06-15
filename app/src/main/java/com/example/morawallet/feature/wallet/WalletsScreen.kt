package com.example.morawallet.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.WalletCard
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@Composable
fun WalletsScreen(
    onWalletClick: (String) -> Unit,
    onAddWallet: () -> Unit,
) {
    val viewModel = moraViewModel { WalletListViewModel(it.walletRepository, it.authRepository) }
    val state = viewModel.state

    when {
        state.loading -> LoadingView()
        state.error != null -> ErrorView(state.error!!)
        state.wallets.isEmpty() -> EmptyView(
            title = "No wallets yet",
            subtitle = "Create your first currency wallet to start tracking money.",
            icon = Icons.Filled.AccountBalanceWallet,
            actionLabel = "Create wallet",
            onAction = onAddWallet,
        )

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                WalletSummaryCard(walletCount = state.wallets.size, onAddWallet = onAddWallet)
            }
            items(state.wallets, key = { it.id }) { wallet ->
                WalletCard(
                    name = wallet.name,
                    currencyCode = wallet.currencyCode,
                    balance = wallet.balance,
                    colorIndex = wallet.colorIndex,
                    onClick = { onWalletClick(wallet.id) },
                )
            }
        }
    }
}

@Composable
private fun WalletSummaryCard(walletCount: Int, onAddWallet: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Spacing.sm))
                androidx.compose.foundation.layout.Column {
                    Text(
                        "$walletCount wallet${if (walletCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Tap a wallet to view details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(onClick = onAddWallet) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Wallet")
            }
        }
    }
}
