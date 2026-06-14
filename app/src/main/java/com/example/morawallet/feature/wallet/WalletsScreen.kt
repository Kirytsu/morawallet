package com.example.morawallet.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.WalletCard
import com.example.morawallet.di.moraViewModel
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Button(
                    onClick = onAddWallet,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add wallet", modifier = Modifier.padding(start = 6.dp))
                }
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
