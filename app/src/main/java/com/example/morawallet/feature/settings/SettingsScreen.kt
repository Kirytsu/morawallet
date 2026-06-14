package com.example.morawallet.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.LetterAvatar
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.core.ui.components.LocalSnackbarController
import com.example.morawallet.core.ui.components.MoraButton
import com.example.morawallet.core.ui.components.MoraConfirmDialog
import com.example.morawallet.core.ui.components.MoraErrorBanner
import com.example.morawallet.core.ui.components.MoraTextField
import com.example.morawallet.core.util.Currencies
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel = moraViewModel { SettingsViewModel(it.authRepository, it.userRepository) }
    val state = viewModel.state
    val snackbar = LocalSnackbarController.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.changeSuccess) {
        if (state.changeSuccess) {
            showPasswordDialog = false
            snackbar?.success("Password updated")
            viewModel.consumeChangeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            LoadingView(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SectionLabel("Account")
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LetterAvatar(
                        letter = state.name.ifBlank { state.email.ifBlank { "?" } },
                        color = MaterialTheme.colorScheme.primary,
                        sizeDp = 52,
                    )
                    Column(modifier = Modifier.padding(start = Spacing.md).weight(1f)) {
                        Text(
                            text = state.name.ifBlank { "Your profile" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            SectionLabel("Preferences")
            Card(Modifier.fillMaxWidth()) {
                BaseCurrencyRow(
                    selected = state.baseCurrency,
                    onSelect = viewModel::setBaseCurrency,
                )
            }

            SectionLabel("Security")
            Card(Modifier.fillMaxWidth()) {
                SettingRow(
                    title = "Change password",
                    value = "",
                    icon = Icons.Filled.Lock,
                    onClick = { showPasswordDialog = true },
                )
            }

            if (state.error != null) {
                MoraErrorBanner(state.error)
            }

            MoraButton(
                text = "Log out",
                onClick = { showLogoutDialog = true },
                leadingIcon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }

    if (showLogoutDialog) {
        MoraConfirmDialog(
            title = "Log out?",
            message = "You will need to sign in again to access your wallets.",
            confirmLabel = "Log out",
            icon = Icons.AutoMirrored.Filled.Logout,
            destructive = true,
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                onLoggedOut()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            loading = state.changing,
            error = state.changeError,
            onDismiss = { showPasswordDialog = false },
            onSubmit = viewModel::changePassword,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(Spacing.md))
            }
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (value.isNotEmpty()) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BaseCurrencyRow(
    selected: String,
    onSelect: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Base currency", style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                selected,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Base currency") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(Currencies.ALL) { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(currency.code)
                                    showPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                currency.code,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.width(64.dp),
                            )
                            Text(
                                currency.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (current: String, new: String, confirm: String) -> Unit,
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                MoraTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = "Current password",
                    isPassword = true,
                )
                MoraTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New password",
                    isPassword = true,
                )
                MoraTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = "Confirm new password",
                    isPassword = true,
                )
                if (error != null) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(current, newPassword, confirm) },
                enabled = !loading,
            ) {
                Text(if (loading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
