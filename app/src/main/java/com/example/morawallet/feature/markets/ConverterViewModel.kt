package com.example.morawallet.feature.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.repository.ExchangeRateRepository
import kotlinx.coroutines.launch

data class ConverterUiState(
    val amount: String = "1",
    val from: String = "USD",
    val to: String = "EUR",
    val result: Double? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class ConverterViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
) : ViewModel() {

    var state by mutableStateOf(ConverterUiState())
        private set

    private var ratesForFrom: Map<String, Double> = emptyMap()

    init {
        loadRatesFor(state.from)
    }

    private fun loadRatesFor(from: String) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            when (val result = exchangeRateRepository.getLatestRates(from)) {
                is Resource.Success -> {
                    ratesForFrom = result.data.rates
                    state = state.copy(loading = false)
                    recompute()
                }

                is Resource.Error -> state = state.copy(loading = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun onAmountChange(value: String) {
        state = state.copy(amount = value)
        recompute()
    }

    fun onFromChange(code: String) {
        state = state.copy(from = code)
        loadRatesFor(code)
    }

    fun onToChange(code: String) {
        state = state.copy(to = code)
        recompute()
    }

    fun swap() {
        val newFrom = state.to
        val newTo = state.from
        state = state.copy(from = newFrom, to = newTo)
        loadRatesFor(newFrom)
    }

    private fun recompute() {
        val amount = state.amount.replace(",", "").trim().toDoubleOrNull()
        if (amount == null) {
            state = state.copy(result = null)
            return
        }
        val result = if (state.from == state.to) {
            amount
        } else {
            ratesForFrom[state.to]?.let { amount * it }
        }
        state = state.copy(result = result)
    }
}
