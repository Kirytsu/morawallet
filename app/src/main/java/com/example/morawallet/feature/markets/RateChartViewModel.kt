package com.example.morawallet.feature.markets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.RatePoint
import com.example.morawallet.data.repository.ExchangeRateRepository
import kotlinx.coroutines.launch

data class RateChartUiState(
    val loading: Boolean = true,
    val points: List<RatePoint> = emptyList(),
    val days: Int = 30,
    val error: String? = null,
    val displayBase: String = "",
    val displayQuote: String = "",
    val isInverted: Boolean = false,
)

class RateChartViewModel(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val base: String,
    private val quote: String,
) : ViewModel() {

    var state by mutableStateOf(RateChartUiState(displayBase = base, displayQuote = quote))
        private set

    init {
        load()
    }

    fun setRange(days: Int) {
        state = state.copy(days = days)
        load()
    }

    fun load() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            state = when (val result = exchangeRateRepository.getTimeseries(base, quote, state.days)) {
                is Resource.Success -> {
                    val raw = result.data
                    val latestRate = raw.lastOrNull()?.value ?: 1.0
                    val invert = latestRate > 0.0 && latestRate < 0.01
                    val points = if (invert) raw.map { RatePoint(it.date, 1.0 / it.value) } else raw
                    state.copy(
                        loading = false,
                        points = points,
                        isInverted = invert,
                        displayBase = if (invert) quote else base,
                        displayQuote = if (invert) base else quote,
                    )
                }
                is Resource.Error -> state.copy(loading = false, error = result.message)
                Resource.Loading -> state
            }
        }
    }
}
