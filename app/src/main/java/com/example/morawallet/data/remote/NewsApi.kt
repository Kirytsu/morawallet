package com.example.morawallet.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class NewsResponseDto(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<ArticleDto> = emptyList(),
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class ArticleDto(
    val source: SourceDto? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String = "",
    val urlToImage: String? = null,
    val publishedAt: String = "",
    val content: String? = null,
)

@Serializable
data class SourceDto(val name: String? = null)

/** NewsAPI.org — requires a free developer API key. */
interface NewsApi {

    @GET("v2/everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 50,
    ): NewsResponseDto
}
