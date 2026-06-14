package com.example.morawallet.data.repository

import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.RatePoint
import com.example.morawallet.data.model.RatesSnapshot
import com.example.morawallet.data.remote.ExchangeRateApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): Resource<RatesSnapshot>
    suspend fun convert(amount: Double, from: String, to: String): Resource<Double>
    suspend fun getTimeseries(from: String, to: String, days: Int): Resource<List<RatePoint>>
}

class FrankfurterExchangeRateRepository(
    private val api: ExchangeRateApi,
) : ExchangeRateRepository {

    override suspend fun getLatestRates(base: String): Resource<RatesSnapshot> = try {
        val dto = api.getLatest(from = base)
        Resource.Success(RatesSnapshot(dto.base.ifEmpty { base }, dto.date, dto.rates))
    } catch (e: Exception) {
        Resource.Error(networkMessage(e), e)
    }

    override suspend fun convert(amount: Double, from: String, to: String): Resource<Double> = try {
        if (from == to) {
            Resource.Success(amount)
        } else {
            val dto = api.getLatest(from = from, to = to)
            val rate = dto.rates[to]
            if (rate != null) Resource.Success(amount * rate) else Resource.Error("Rate unavailable for $to")
        }
    } catch (e: Exception) {
        Resource.Error(networkMessage(e), e)
    }

    override suspend fun getTimeseries(from: String, to: String, days: Int): Resource<List<RatePoint>> =
        try {
            if (from == to) {
                Resource.Success(emptyList())
            } else {
                val (start, end) = rangeFor(days)
                val dto = api.getTimeseries(range = "$start..$end", from = from, to = to)
                val points = dto.rates.entries
                    .sortedBy { it.key }
                    .mapNotNull { entry -> entry.value[to]?.let { RatePoint(entry.key, it) } }
                Resource.Success(points)
            }
        } catch (e: Exception) {
            Resource.Error(networkMessage(e), e)
        }

    private fun rangeFor(days: Int): Pair<String, String> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val end = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val start = format.format(calendar.time)
        return start to end
    }

    private fun networkMessage(e: Exception): String =
        "Could not load exchange rates. Check your connection and try again."
}
