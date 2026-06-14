package com.example.morawallet.feature.news

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morawallet.core.util.Resource
import com.example.morawallet.data.model.NewsArticle
import com.example.morawallet.data.repository.NewsRepository
import kotlinx.coroutines.launch

data class NewsUiState(
    val loading: Boolean = true,
    val articles: List<NewsArticle> = emptyList(),
    val error: String? = null,
    val selectedCategory: Int = 0,
    val search: String = "",
)

class NewsViewModel(
    private val newsRepository: NewsRepository,
) : ViewModel() {

    val categories: List<Pair<String, String>> = listOf(
        "Economy" to "economy OR inflation OR \"central bank\"",
        "Currency" to "currency OR forex OR \"exchange rate\"",
        "Markets" to "stock market OR investing OR markets",
        "Business" to "business OR finance",
    )

    var state by mutableStateOf(NewsUiState())
        private set

    init {
        load(categories[0].second)
    }

    fun selectCategory(index: Int) {
        state = state.copy(selectedCategory = index, search = "")
        load(categories[index].second)
    }

    fun onSearchChange(value: String) {
        state = state.copy(search = value)
    }

    fun submitSearch() {
        val query = state.search.trim()
        load(query.ifBlank { categories[state.selectedCategory].second })
    }

    fun refresh() {
        load(state.search.trim().ifBlank { categories[state.selectedCategory].second })
    }

    private fun load(query: String) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            state = when (val result = newsRepository.getNews(query)) {
                is Resource.Success -> state.copy(loading = false, articles = result.data)
                is Resource.Error -> state.copy(loading = false, error = result.message)
                Resource.Loading -> state
            }
        }
    }
}
