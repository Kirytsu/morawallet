package com.example.morawallet.feature.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Currencies
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.ExchangeRateRepository
import com.example.morawallet.data.repository.UserRepository
import kotlinx.coroutines.launch

data class MarketsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val rates: Map<String, Double> = emptyMap(),
    val date: String = "",
    val query: String = "",
    val error: String? = null,
) {
    val visibleRates: List<Pair<String, Double>>
        get() = rates.entries
            .filter {
                query.isBlank() ||
                    it.key.contains(query, ignoreCase = true) ||
                    Currencies.displayName(it.key).contains(query, ignoreCase = true)
            }
            .map { it.key to it.value }
            .sortedBy { it.first }
}

class MarketsViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(MarketsUiState())
        private set

    init {
        viewModelScope.launch {
            val base = loadBaseCurrency()
            state = state.copy(baseCurrency = base)
            loadRates(base)
        }
    }

    private suspend fun loadBaseCurrency(): String {
        val uid = authRepository.currentUserId() ?: return "USD"
        return when (val result = userRepository.getUser(uid)) {
            is Resource.Success -> result.data.baseCurrency
            else -> "USD"
        }
    }

    private suspend fun loadRates(base: String) {
        state = state.copy(loading = true, error = null)
        state = when (val result = exchangeRateRepository.getLatestRates(base)) {
            is Resource.Success -> state.copy(
                loading = false,
                rates = result.data.rates,
                date = result.data.date,
            )

            is Resource.Error -> state.copy(loading = false, error = result.message)
            Resource.Loading -> state
        }
    }

    fun refresh() {
        viewModelScope.launch { loadRates(state.baseCurrency) }
    }

    fun setQuery(value: String) {
        state = state.copy(query = value)
    }
}
