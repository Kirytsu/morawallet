package com.example.morawallet.feature.news

import androidx.lifecycle.ViewModel
import com.example.morawallet.data.model.NewsArticle
import com.example.morawallet.data.repository.NewsRepository

class NewsDetailViewModel(
    newsRepository: NewsRepository,
    articleId: String,
) : ViewModel() {
    val article: NewsArticle? = newsRepository.getCachedArticle(articleId)
}
