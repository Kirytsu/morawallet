package com.example.morawallet.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.ui.components.MoraButton
import com.example.morawallet.core.ui.components.MoraErrorBanner
import com.example.morawallet.core.ui.components.MoraTextField
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.Spacing

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val viewModel = moraViewModel { LoginViewModel(it.authRepository, it.userRepository) }
    val state = viewModel.state

    LaunchedEffect(state.success) {
        if (state.success) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandMark()
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = Spacing.xl),
        )
        Text(
            text = "Log in to your wallet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xl),
        )

        MoraTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            placeholder = "you@example.com",
            leadingIcon = Icons.Filled.Email,
            error = state.emailError,
            keyboardType = KeyboardType.Email,
        )
        Box(Modifier.size(Spacing.md))
        MoraTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            error = state.passwordError,
            isPassword = true,
        )

        if (state.error != null) {
            MoraErrorBanner(
                state.error,
                modifier = Modifier.padding(top = Spacing.md),
            )
        }

        MoraButton(
            text = "Log in",
            onClick = viewModel::login,
            loading = state.loading,
            leadingIcon = Icons.AutoMirrored.Filled.Login,
            modifier = Modifier.padding(top = Spacing.xl),
        )

        GoogleSignInSection(
            loading = state.googleLoading,
            enabled = !state.loading,
            onStart = viewModel::onGoogleStart,
            onToken = viewModel::signInWithGoogle,
            onCancelled = viewModel::onGoogleCancelled,
            onError = viewModel::onGoogleError,
            modifier = Modifier.padding(top = Spacing.sm),
        )

        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Register")
            }
        }
    }
}

@Composable
internal fun BrandMark() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(88.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.example.morawallet.R.drawable.ic_mora_logo),
                contentDescription = "MoraWallet logo",
                modifier = Modifier.size(54.dp),
            )
        }
    }
}
