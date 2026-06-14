package com.example.morawallet.feature.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.DateUtils
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.TransactionRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val loading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val error: String? = null,
    val recordType: TransactionType = TransactionType.INCOME,
    val selectedCategory: String? = null,
    val walletFilter: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val query: String = "",
) {
    val walletsById: Map<String, Wallet> get() = wallets.associateBy { it.id }

    val analysisTransactions: List<Transaction>
        get() = transactions.filter { txn ->
                (startDate == null || txn.date >= DateUtils.startOfDay(startDate)) &&
                (endDate == null || txn.date <= DateUtils.endOfDay(endDate)) &&
                txn.typeEnum == recordType &&
                (walletFilter == null || txn.walletId == walletFilter || txn.toWalletId == walletFilter) &&
                matchesQuery(txn)
        }

    val visibleTransactions: List<Transaction>
        get() = analysisTransactions.filter { txn ->
            selectedCategory == null || txn.category.ifBlank { "Other" } == selectedCategory
        }

    val activeCurrency: String?
        get() = walletFilter?.let { walletsById[it]?.currencyCode }
            ?: analysisTransactions.map { it.currencyCode }.distinct().singleOrNull()

    val categoryBreakdown: List<Pair<String, Double>>
        get() = analysisTransactions
            .filter { it.typeEnum != TransactionType.TRANSFER }
            .groupBy { it.category.ifBlank { "Other" } }
            .map { (cat, list) -> cat to list.sumOf { it.amount } }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }

    private fun matchesQuery(txn: Transaction): Boolean {
        if (query.isBlank()) return true
        val source = walletsById[txn.walletId]?.name.orEmpty()
        val dest = walletsById[txn.toWalletId]?.name.orEmpty()
        return txn.note.contains(query, ignoreCase = true) ||
            txn.category.contains(query, ignoreCase = true) ||
            source.contains(query, ignoreCase = true) ||
            dest.contains(query, ignoreCase = true) ||
            txn.currencyCode.contains(query, ignoreCase = true)
    }
}

class TransactionListViewModel(
    transactionRepository: TransactionRepository,
    walletRepository: WalletRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(TransactionsUiState())
        private set

    init {
        val uid = authRepository.currentUserId()
        if (uid == null) {
            state = state.copy(loading = false, error = "You are not signed in")
        } else {
            viewModelScope.launch {
                combine(
                    transactionRepository.observeTransactions(uid),
                    walletRepository.observeWallets(uid),
                ) { txns, wallets -> txns to wallets }
                    .catch { e -> state = state.copy(loading = false, error = e.localizedMessage) }
                    .collect { (txns, wallets) ->
                        state = state.copy(
                            loading = false,
                            transactions = txns,
                            wallets = wallets,
                            error = null,
                        )
                    }
            }
        }
    }

    fun setRecordType(type: TransactionType) {
        state = state.copy(recordType = type, selectedCategory = null)
    }

    fun setWalletFilter(walletId: String?) {
        state = state.copy(walletFilter = walletId, selectedCategory = null)
    }

    fun setStartDate(millis: Long?) {
        state = state.copy(startDate = millis?.let(DateUtils::startOfDay), selectedCategory = null)
    }

    fun setEndDate(millis: Long?) {
        state = state.copy(endDate = millis?.let(DateUtils::startOfDay), selectedCategory = null)
    }

    fun clearDates() {
        state = state.copy(startDate = null, endDate = null, selectedCategory = null)
    }

    fun setQuery(value: String) {
        state = state.copy(query = value)
    }

    fun setSelectedCategory(category: String?) {
        state = state.copy(selectedCategory = category)
    }
}
