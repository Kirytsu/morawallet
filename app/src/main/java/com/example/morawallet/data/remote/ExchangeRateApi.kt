package com.example.morawallet.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class LatestRatesDto(
    val amount: Double = 1.0,
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
)

@Serializable
data class TimeseriesDto(
    val amount: Double = 1.0,
    val base: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    val rates: Map<String, Map<String, Double>> = emptyMap(),
)

/** Frankfurter API (https://www.frankfurter.app) — free, no key, ECB fiat rates. */
interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatest(
        @Query("from") from: String,
        @Query("to") to: String? = null,
    ): LatestRatesDto

    /** [range] is "yyyy-MM-dd..yyyy-MM-dd". */
    @GET("{range}")
    suspend fun getTimeseries(
        @Path("range", encoded = true) range: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): TimeseriesDto
}
