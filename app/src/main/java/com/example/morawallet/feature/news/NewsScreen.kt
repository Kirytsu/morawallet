package com.example.morawallet.feature.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.ErrorView
import com.example.morawallet.core.ui.components.LoadingView
import com.example.morawallet.data.model.NewsArticle
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.MoraTheme
import com.example.morawallet.ui.theme.Spacing

@Composable
fun NewsScreen(
    onArticleClick: (String) -> Unit,
) {
    val viewModel = moraViewModel { NewsViewModel(it.newsRepository) }
    val state = viewModel.state

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.search,
            onValueChange = viewModel::onSearchChange,
            label = { Text("Search news") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            modifier = Modifier.padding(bottom = Spacing.sm),
        ) {
            itemsIndexed(viewModel.categories) { index, category ->
                val color = MoraTheme.colors.chart[index % MoraTheme.colors.chart.size]
                val selected = state.selectedCategory == index && state.search.isBlank()
                Button(
                    onClick = { viewModel.selectCategory(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) color else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(category.first)
                }
            }
        }

        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(state.error!!, onRetry = viewModel::refresh)
            state.articles.isEmpty() -> EmptyView(title = "No news found")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                itemsIndexed(
                    items = state.articles,
                    key = { _, article -> article.id },
                    span = { index, _ ->
                        if (index == 0 || index % 7 == 0) GridItemSpan(2) else GridItemSpan(1)
                    },
                ) { index, article ->
                    NewsCard(
                        article = article,
                        featured = index == 0 || index % 7 == 0,
                        color = MoraTheme.colors.chart[index % MoraTheme.colors.chart.size],
                        onClick = { onArticleClick(article.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    article: NewsArticle,
    featured: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (featured) MaterialTheme.colorScheme.tertiaryContainer else MoraTheme.colors.surfaceRaised),
        border = BorderStroke(1.dp, if (featured) color else MoraTheme.colors.borderSubtle),
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (featured) Modifier.height(150.dp) else Modifier.aspectRatio(1.25f))
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (featured) Modifier.height(150.dp) else Modifier.aspectRatio(1.25f))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Newspaper, contentDescription = null, tint = color, modifier = Modifier.size(34.dp))
                }
            }

            Text(
                text = article.title,
                style = if (featured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (featured) 2 else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(Icons.Filled.Newspaper, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(
                    text = listOf(article.source, article.publishedAt.take(10))
                        .filter { it.isNotBlank() }
                        .joinToString(" - "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
