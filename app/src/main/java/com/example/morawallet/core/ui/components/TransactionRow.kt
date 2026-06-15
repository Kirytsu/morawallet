package com.example.morawallet.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.util.Categories
import com.example.morawallet.core.util.CurrencyFormatter
import com.example.morawallet.core.util.DateUtils
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.paletteColor

@Composable
fun TransactionRow(
    transaction: Transaction,
    walletsById: Map<String, Wallet>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceName = walletsById[transaction.walletId]?.name ?: "-"
    val title = transaction.category.ifBlank { transaction.note.ifBlank { "Transaction" } }
    val subtitle = when (transaction.typeEnum) {
        TransactionType.TRANSFER -> {
            val dest = walletsById[transaction.toWalletId]?.name ?: "-"
            "$sourceName → $dest"
        }
        else -> sourceName
    }
    // Show note on its own line only when the title already shows the category
    val showNote = transaction.note.isNotBlank() && transaction.category.isNotBlank()
    val amountText = when (transaction.typeEnum) {
        TransactionType.INCOME -> "+" + CurrencyFormatter.format(transaction.amount, transaction.currencyCode)
        TransactionType.EXPENSE -> "-" + CurrencyFormatter.format(transaction.amount, transaction.currencyCode)
        TransactionType.TRANSFER -> CurrencyFormatter.format(transaction.amount, transaction.currencyCode)
    }
    val amountColor = when (transaction.typeEnum) {
        TransactionType.INCOME -> MoraTheme.colors.income
        TransactionType.EXPENSE -> MoraTheme.colors.expense
        TransactionType.TRANSFER -> MoraTheme.colors.transfer
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MoraTheme.colors.surfaceRaised),
        border = BorderStroke(1.dp, MoraTheme.colors.borderSubtle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAvatar(
                icon = Categories.icon(transaction.category),
                color = paletteColor(Categories.colorIndex(transaction.category)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${DateUtils.formatTime(transaction.date)} · $subtitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showNote) {
                    Text(
                        transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.labelLarge,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 132.dp),
            )
        }
    }
}
