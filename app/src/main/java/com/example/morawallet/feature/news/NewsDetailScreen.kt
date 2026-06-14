package com.example.morawallet.feature.news

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.morawallet.core.ui.components.EmptyView
import com.example.morawallet.core.ui.components.MoraButton
import com.example.morawallet.di.moraViewModel
import com.example.morawallet.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    articleId: String,
    onBack: () -> Unit,
) {
    val viewModel = moraViewModel { NewsDetailViewModel(it.newsRepository, articleId) }
    val article = viewModel.article
    val context = LocalContext.current

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (article == null) {
            EmptyView(
                title = "Article unavailable",
                subtitle = "Open it again from the news list.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(article.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = listOf(article.source, article.publishedAt.take(10))
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.md),
                )
                if (article.summary.isNotBlank()) {
                    Text(article.summary, style = MaterialTheme.typography.bodyLarge)
                }
                if (article.content.isNotBlank()) {
                    Text(
                        article.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                } else if (article.summary.isBlank()) {
                    Text(
                        "No preview available for this article. Open it to read the full story.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.md),
                    )
                }
                MoraButton(
                    text = "Read full article",
                    leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, article.url.toUri()))
                        }
                    },
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }
        }
    }
}
