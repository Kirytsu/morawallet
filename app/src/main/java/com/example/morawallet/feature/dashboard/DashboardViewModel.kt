package com.example.morawallet.feature.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.CategorySlice
import com.example.morawallet.core.util.PeriodBar
import com.example.morawallet.core.util.ReportAggregations
import com.example.morawallet.core.util.ReportRange
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.Transaction
import com.example.morawallet.data.model.TransactionType
import com.example.morawallet.data.model.Wallet
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.ExchangeRateRepository
import com.example.morawallet.data.repository.TransactionRepository
import com.example.morawallet.data.repository.UserRepository
import com.example.morawallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val portfolioValue: Double = 0.0,
    val wallets: List<Wallet> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val walletsById: Map<String, Wallet> = emptyMap(),
    val rates: Map<String, Double> = emptyMap(),
    val range: ReportRange = ReportRange.MONTH,
    val categoryReportType: TransactionType = TransactionType.INCOME,
    val incomeExpenseSeries: List<PeriodBar> = emptyList(),
    val categoryBreakdown: List<CategorySlice> = emptyList(),
    val error: String? = null,
) {
    val rangeIncome: Double get() = incomeExpenseSeries.sumOf { it.income }
    val rangeExpense: Double get() = incomeExpenseSeries.sumOf { it.expense }
}

class DashboardViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(DashboardUiState())
        private set

    private var allTransactions: List<Transaction> = emptyList()

    init {
        val uid = authRepository.currentUserId()
        if (uid == null) {
            state = state.copy(loading = false, error = "You are not signed in")
        } else {
            viewModelScope.launch {
                val base = loadBaseCurrency(uid)
                val rates = loadRates(base)
                state = state.copy(baseCurrency = base, rates = rates)

                combine(
                    walletRepository.observeWallets(uid),
                    transactionRepository.observeTransactions(uid),
                ) { wallets, txns -> wallets to txns }
                    .collect { (wallets, txns) ->
                        allTransactions = txns
                        state = state.copy(
                            loading = false,
                            wallets = wallets,
                            walletsById = wallets.associateBy { it.id },
                            recentTransactions = txns.take(8),
                            portfolioValue = computePortfolio(wallets, state.baseCurrency, state.rates),
                        ).withReports()
                    }
            }
        }
    }

    fun setRange(range: ReportRange) {
        state = state.copy(range = range).withReports()
    }

    fun setCategoryReportType(type: TransactionType) {
        if (type == TransactionType.INCOME || type == TransactionType.EXPENSE) {
            state = state.copy(categoryReportType = type).withReports()
        }
    }

    /** Recomputes the report series/breakdown for the current range from [allTransactions]. */
    private fun DashboardUiState.withReports(): DashboardUiState {
        val now = System.currentTimeMillis()
        return copy(
            incomeExpenseSeries = ReportAggregations.incomeExpenseSeries(allTransactions, range, now, ::toBase),
            categoryBreakdown = ReportAggregations.categoryBreakdown(
                allTransactions,
                range,
                now,
                categoryReportType,
                ::toBase,
            ),
        )
    }

    /** FX-aware conversion of an amount in [code] into the user's base currency. */
    private fun toBase(amount: Double, code: String): Double {
        val base = state.baseCurrency
        val rates = state.rates
        return when {
            code == base -> amount
            rates[code] != null -> amount / rates.getValue(code)
            else -> amount // no rate available (e.g. offline): keep the figure rather than drop it
        }
    }

    private suspend fun loadBaseCurrency(uid: String): String =
        when (val result = userRepository.getUser(uid)) {
            is Resource.Success -> result.data.baseCurrency
            else -> "USD"
        }

    private suspend fun loadRates(base: String): Map<String, Double> =
        when (val result = exchangeRateRepository.getLatestRates(base)) {
            is Resource.Success -> result.data.rates
            else -> emptyMap()
        }

    /** Converts each wallet balance into the base currency and sums them. */
    private fun computePortfolio(
        wallets: List<Wallet>,
        base: String,
        rates: Map<String, Double>,
    ): Double = wallets.sumOf { wallet ->
        when {
            wallet.currencyCode == base -> wallet.balance
            rates[wallet.currencyCode] != null -> wallet.balance / rates.getValue(wallet.currencyCode)
            else -> 0.0
        }
    }

    fun refresh() {
        val base = state.baseCurrency
        viewModelScope.launch {
            val rates = loadRates(base)
            state = state.copy(
                rates = rates,
                portfolioValue = computePortfolio(state.wallets, base, rates),
            ).withReports()
        }
    }
}
