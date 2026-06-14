package com.example.morawallet.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.morawallet.ui.theme.MoraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Severity of an in-app message, driving its color + icon. */
enum class MessageType { SUCCESS, ERROR, INFO, WARNING }

/** Custom [SnackbarVisuals] that carries the [MessageType]. */
private class MoraSnackbarVisuals(
    override val message: String,
    val type: MessageType,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals

/** App-wide entry point to raise styled snackbars from anywhere in the UI tree. */
class SnackbarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    fun show(message: String, type: MessageType = MessageType.INFO) {
        scope.launch {
            hostState.showSnackbar(MoraSnackbarVisuals(message = message, type = type))
        }
    }

    fun success(message: String) = show(message, MessageType.SUCCESS)
    fun error(message: String) = show(message, MessageType.ERROR)
}

@Composable
fun rememberSnackbarController(): SnackbarController {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) { SnackbarController(hostState, scope) }
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController?> { null }

/** Styled host that renders MoraWallet's colored, icon-led snackbars. */
@Composable
fun MoraSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val visuals = data.visuals
        val type = (visuals as? MoraSnackbarVisuals)?.type ?: MessageType.INFO
        val (container, content, icon) = messageStyle(type)

        Snackbar(
            containerColor = container,
            contentColor = content,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(visuals.message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private data class MessageStyle(val container: Color, val content: Color, val icon: ImageVector)

@Composable
private fun messageStyle(type: MessageType): MessageStyle = when (type) {
    MessageType.SUCCESS -> MessageStyle(MoraTheme.colors.income, Color.White, Icons.Filled.CheckCircle)
    MessageType.ERROR -> MessageStyle(MoraTheme.colors.expense, Color.White, Icons.Filled.Error)
    MessageType.WARNING -> MessageStyle(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onTertiary,
        Icons.Filled.WarningAmber,
    )
    MessageType.INFO -> MessageStyle(
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface,
        Icons.Filled.Info,
    )
}
