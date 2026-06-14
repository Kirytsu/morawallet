package com.example.morawallet.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Line chart you can scrub. Drag (or tap) anywhere to lock onto the nearest point;
 * the header shows that point's label + value, a crosshair marks it on the plot, and
 * the Y axis shows min/mid/max value ticks. Solves "the chart has no numbers and you
 * can't check a point".
 */
@Composable
fun InteractiveLineChart(
    values: List<Double>,
    labels: List<String>,
    lineColor: Color,
    valueFormat: (Double) -> String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 220.dp,
) {
    if (values.size < 2) return

    var selected by remember(values) { mutableIntStateOf(values.lastIndex) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val crosshairColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(color = axisTextColor, fontSize = 10.sp)

    val maxValue = values.max()
    val minValue = values.min()

    Column(modifier = modifier.fillMaxWidth()) {
        // Readout header — the selected point's date + price.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = labels.getOrElse(selected) { "" },
                    style = MaterialTheme.typography.labelMedium,
                    color = axisTextColor,
                )
                Text(
                    text = valueFormat(values[selected]),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 8.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .pointerInput(values) {
                        detectTapGestures { offset ->
                            selected = nearestIndex(offset.x, size.width.toFloat(), values.size)
                        }
                    }
                    .pointerInput(values) {
                        detectDragGestures { change, _ ->
                            selected = nearestIndex(change.position.x, size.width.toFloat(), values.size)
                        }
                    },
            ) {
                val leftPad = 72f
                val rightPad = 12f
                val plotW = size.width - leftPad - rightPad
                val plotH = size.height
                val span = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }
                val stepX = plotW / (values.size - 1)
                fun xFor(i: Int) = leftPad + i * stepX
                fun yFor(v: Double) = (plotH - ((v - minValue) / span) * plotH).toFloat()

                // Horizontal grid + Y value ticks (min / mid / max)
                val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                listOf(0f, 0.5f, 1f).forEach { frac ->
                    val y = plotH * frac
                    drawLine(gridColor, Offset(leftPad, y), Offset(size.width - rightPad, y), 1.5f, pathEffect = dashed)
                    val value = maxValue - (maxValue - minValue) * frac
                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueFormat(value),
                        topLeft = Offset(0f, (y - 7f).coerceIn(0f, plotH - 14f)),
                        style = axisStyle,
                    )
                }

                // Line
                val line = Path()
                values.forEachIndexed { i, v ->
                    val x = xFor(i); val y = yFor(v)
                    if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
                }
                drawPath(line, lineColor, style = Stroke(width = 5f))

                // Crosshair on the selected point
                val sx = xFor(selected)
                val sy = yFor(values[selected])
                drawLine(crosshairColor.copy(alpha = 0.4f), Offset(sx, 0f), Offset(sx, plotH), 2f)
                drawCircle(Color.White, radius = 9f, center = Offset(sx, sy))
                drawCircle(lineColor, radius = 6f, center = Offset(sx, sy))
            }
        }

        // X axis date labels: first / middle / last
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(shortLabel(labels.first()), style = MaterialTheme.typography.labelSmall, color = axisTextColor)
            Text(shortLabel(labels[labels.size / 2]), style = MaterialTheme.typography.labelSmall, color = axisTextColor)
            Text(shortLabel(labels.last()), style = MaterialTheme.typography.labelSmall, color = axisTextColor)
        }
    }
}

private fun nearestIndex(x: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    val stepX = width / (count - 1)
    return (x / stepX).toInt().coerceIn(0, count - 1)
}

private fun shortLabel(label: String): String =
    if (label.length >= 10 && label[4] == '-' && label[7] == '-') label.substring(5) else label.take(8)
