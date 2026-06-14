package com.example.morawallet.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.morawallet.core.util.PeriodBar

/**
 * Grouped bar chart: two bars (income, expense) per period. Pure-Canvas bars with a
 * baseline; period labels render as Compose text below so we avoid canvas text APIs.
 */
@Composable
fun IncomeExpenseBarChart(
    bars: List<PeriodBar>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 160.dp,
) {
    val maxValue = bars.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 0.0
    val baseline = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            if (bars.isEmpty() || maxValue <= 0.0) return@Canvas
            val groupWidth = size.width / bars.size
            val barWidth = (groupWidth * 0.28f).coerceAtMost(28f)
            val gap = barWidth * 0.35f
            val usableHeight = size.height - 8f
            val corner = barWidth * 0.4f

            // Baseline
            drawLine(
                color = baseline,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f,
            )

            bars.forEachIndexed { index, bar ->
                val center = groupWidth * index + groupWidth / 2f
                val incomeH = (bar.income / maxValue * usableHeight).toFloat()
                val expenseH = (bar.expense / maxValue * usableHeight).toFloat()

                val incomeLeft = center - gap / 2f - barWidth
                val expenseLeft = center + gap / 2f

                if (incomeH > 0f) {
                    drawRoundRect(
                        color = incomeColor,
                        topLeft = Offset(incomeLeft, size.height - incomeH),
                        size = Size(barWidth, incomeH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                    )
                }
                if (expenseH > 0f) {
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(expenseLeft, size.height - expenseH),
                        size = Size(barWidth, expenseH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
