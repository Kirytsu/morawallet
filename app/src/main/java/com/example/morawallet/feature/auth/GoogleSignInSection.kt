package com.example.morawallet.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.morawallet.data.auth.GoogleAuthClient
import com.example.morawallet.data.auth.GoogleTokenResult
import com.example.morawallet.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * "OR / Continue with Google" block shared by the login and register screens.
 * Owns the Credential Manager call (needs an Activity context) and reports the
 * outcome back to the caller's ViewModel via the provided callbacks.
 */
@Composable
fun GoogleSignInSection(
    loading: Boolean,
    enabled: Boolean,
    onStart: () -> Unit,
    onToken: (String) -> Unit,
    onCancelled: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleClient = remember(context) { GoogleAuthClient(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "OR",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }

    OutlinedButton(
        onClick = {
            scope.launch {
                onStart()
                when (val result = googleClient.getIdToken()) {
                    is GoogleTokenResult.Success -> onToken(result.idToken)
                    GoogleTokenResult.Cancelled -> onCancelled()
                    is GoogleTokenResult.Failure -> onError(result.message)
                }
            }
        },
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
        } else {
            Text("Continue with Google")
        }
    }
}
