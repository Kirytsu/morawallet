package com.example.morawallet.feature.wallet

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.AmountField
import com.example.morawallet.core.ui.components.CircleAvatar
import com.example.morawallet.core.ui.components.CurrencyPicker
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraButton
import com.example.morawallet.core.ui.components.MoraErrorBanner
import com.example.morawallet.core.ui.components.MoraTextField
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.CategoryColors
import com.example.morawallet.ui.theme.Spacing
import com.example.morawallet.ui.theme.paletteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletFormScreen(
    walletId: String?,
    onBack: () -> Unit,
) {
    val viewModel = moraViewModel {
        WalletFormViewModel(it.walletRepository, it.authRepository, walletId)
    }
    val state = viewModel.state
    val snackbar = LocalSnackbarController.current

    LaunchedEffect(state.success) {
        if (state.success) {
            snackbar?.success(if (state.isEdit) "Wallet updated" else "Wallet created")
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit wallet" else "New wallet") },
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
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            MoraTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Wallet name",
                placeholder = "e.g. Everyday, Savings",
                error = state.nameError,
            )
            CurrencyPicker(
                selected = state.currencyCode,
                onSelect = viewModel::onCurrencyChange,
            )
            if (!state.isEdit) {
                AmountField(
                    value = state.initialBalance,
                    onValueChange = viewModel::onInitialBalanceChange,
                    currencyCode = state.currencyCode,
                    label = "Initial balance (optional)",
                    error = state.balanceError,
                )
            }

            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorPickerRow(
                selectedIndex = state.colorIndex,
                onSelect = viewModel::onColorChange,
            )

            if (state.error != null) {
                MoraErrorBanner(state.error!!)
            }

            MoraButton(
                text = if (state.isEdit) "Save changes" else "Create wallet",
                onClick = viewModel::save,
                loading = state.loading,
                leadingIcon = Icons.Filled.Check,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

@Composable
private fun ColorPickerRow(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        itemsIndexed(CategoryColors) { index, _ ->
            val selected = index == selectedIndex
            CircleAvatar(
                color = paletteColor(index),
                sizeDp = 40,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (selected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(index) },
            ) {}
        }
    }
}
