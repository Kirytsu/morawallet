package com.example.morawallet.data.model

/** A news article shown in the Market News feature. */
data class NewsArticle(
    val id: String,
    val title: String,
    val source: String,
    val summary: String,
    val imageUrl: String?,
    val url: String,
    val publishedAt: String,
    val content: String,
)
