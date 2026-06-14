package com.example.morawallet.data.repository

import com.example.morawallet.core.util.HtmlText
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.NewsArticle
import com.example.morawallet.data.remote.ArticleDto
import com.example.morawallet.data.remote.NewsApi
import kotlin.math.abs

interface NewsRepository {
    suspend fun getNews(query: String): Resource<List<NewsArticle>>
    fun getCachedArticle(id: String): NewsArticle?
}

class NewsApiRepository(
    private val api: NewsApi,
    private val apiKey: String,
) : NewsRepository {

    private val cache = mutableMapOf<String, NewsArticle>()

    override suspend fun getNews(query: String): Resource<List<NewsArticle>> {
        if (apiKey.isBlank()) {
            return Resource.Error(
                "Add a News API key (NEWS_API_KEY in gradle.properties) to load market news.",
            )
        }
        return try {
            val dto = api.everything(query = query, apiKey = apiKey)
            val articles = dto.articles
                .filter { !it.title.isNullOrBlank() && it.title != "[Removed]" }
                .map { it.toDomain() }
            cache.clear()
            articles.forEach { cache[it.id] = it }
            Resource.Success(articles)
        } catch (e: Exception) {
            Resource.Error("Could not load news. Check your connection or API key.", e)
        }
    }

    override fun getCachedArticle(id: String): NewsArticle? = cache[id]

    private fun ArticleDto.toDomain(): NewsArticle = NewsArticle(
        id = abs(url.hashCode()).toString(),
        title = HtmlText.clean(title),
        source = source?.name ?: "Unknown",
        summary = HtmlText.clean(description),
        imageUrl = urlToImage,
        url = url,
        publishedAt = publishedAt,
        content = HtmlText.clean(content),
    )
}
