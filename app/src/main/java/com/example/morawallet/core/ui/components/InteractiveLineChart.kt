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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val L_PAD = 72f
private const val R_PAD = 12f
private const val B_PAD = 26f

@Composable
fun InteractiveLineChart(
    values: List<Double>,
    labels: List<String>,
    lineColor: Color,
    valueFormat: (Double) -> String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 220.dp,
    highColor: Color = Color(0xFF13A079),
    lowColor: Color = Color(0xFFE5485E),
) {
    if (values.size < 2) return

    var selected by remember(values) { mutableIntStateOf(values.lastIndex) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val crosshairColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(color = axisTextColor, fontSize = 10.sp)
    val boldStyle = TextStyle(color = crosshairColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)

    val maxValue = values.max()
    val minValue = values.min()
    val span = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrub readout
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                            val plotW = size.width - L_PAD - R_PAD
                            val stepX = plotW / (values.size - 1)
                            val adj = (offset.x - L_PAD).coerceIn(0f, plotW)
                            selected = (adj / stepX).toInt().coerceIn(0, values.lastIndex)
                        }
                    }
                    .pointerInput(values) {
                        detectDragGestures { change, _ ->
                            val plotW = size.width - L_PAD - R_PAD
                            val stepX = plotW / (values.size - 1)
                            val adj = (change.position.x - L_PAD).coerceIn(0f, plotW)
                            selected = (adj / stepX).toInt().coerceIn(0, values.lastIndex)
                        }
                    },
            ) {
                val plotW = size.width - L_PAD - R_PAD
                val plotH = size.height - B_PAD
                val stepX = plotW / (values.size - 1)
                val yPad = span * 0.10

                fun xFor(i: Int) = L_PAD + i * stepX
                fun yFor(v: Double) = (plotH * (1.0 - (v - minValue + yPad) / (span + yPad * 2))).toFloat()

                val dashed = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                val dotted = PathEffect.dashPathEffect(floatArrayOf(4f, 5f))

                // ── High line (income green) ──────────────────────────────────
                val highY = yFor(maxValue)
                drawLine(highColor, Offset(L_PAD, highY), Offset(size.width - R_PAD, highY), 2f, pathEffect = dashed)
                val highText = "H: ${valueFormat(maxValue)}"
                drawText(
                    textMeasurer, highText,
                    topLeft = Offset(0f, (highY + 3f).coerceIn(0f, plotH - 14f)),
                    style = axisStyle.copy(color = highColor),
                )

                // ── Mid grid (neutral) ────────────────────────────────────────
                val midVal = (maxValue + minValue) / 2.0
                val midY = yFor(midVal)
                drawLine(gridColor, Offset(L_PAD, midY), Offset(size.width - R_PAD, midY), 1f, pathEffect = dashed)
                drawText(
                    textMeasurer, valueFormat(midVal),
                    topLeft = Offset(0f, (midY - 7f).coerceIn(0f, plotH - 14f)),
                    style = axisStyle,
                )

                // ── Low line (expense red) ────────────────────────────────────
                val lowY = yFor(minValue)
                drawLine(lowColor, Offset(L_PAD, lowY), Offset(size.width - R_PAD, lowY), 2f, pathEffect = dashed)
                val lowText = "L: ${valueFormat(minValue)}"
                val lowMeasured = textMeasurer.measure(lowText, style = axisStyle)
                drawText(
                    textMeasurer, lowText,
                    topLeft = Offset(0f, (lowY - lowMeasured.size.height - 3f).coerceIn(0f, plotH - lowMeasured.size.height.toFloat())),
                    style = axisStyle.copy(color = lowColor),
                )

                // ── Main line ─────────────────────────────────────────────────
                val line = Path()
                values.forEachIndexed { i, v ->
                    val x = xFor(i); val y = yFor(v)
                    if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
                }
                drawPath(line, lineColor, style = Stroke(width = 5f))

                // ── Crosshair ─────────────────────────────────────────────────
                val sx = xFor(selected)
                val sy = yFor(values[selected])

                // Vertical dotted line
                drawLine(crosshairColor.copy(alpha = 0.5f), Offset(sx, 0f), Offset(sx, plotH), 2f, pathEffect = dotted)

                // Horizontal dotted line at selected Y
                drawLine(crosshairColor.copy(alpha = 0.35f), Offset(L_PAD, sy), Offset(size.width - R_PAD, sy), 1.5f, pathEffect = dotted)

                // Selected value on left Y-axis
                val valText = valueFormat(values[selected])
                val valMeasured = textMeasurer.measure(valText, style = boldStyle)
                drawText(
                    textMeasurer, valText,
                    topLeft = Offset(0f, (sy - valMeasured.size.height / 2f).coerceIn(0f, plotH - valMeasured.size.height.toFloat())),
                    style = boldStyle,
                )

                // Point dot
                drawCircle(Color.White, radius = 9f, center = Offset(sx, sy))
                drawCircle(lineColor, radius = 6f, center = Offset(sx, sy))

                // ── Date label below chart at selected X ──────────────────────
                val dateText = shortLabel(labels.getOrElse(selected) { "" })
                if (dateText.isNotEmpty()) {
                    val dateMeasured = textMeasurer.measure(dateText, style = boldStyle)
                    val dateX = (sx - dateMeasured.size.width / 2f)
                        .coerceIn(L_PAD, size.width - R_PAD - dateMeasured.size.width)
                    drawText(
                        textMeasurer, dateText,
                        topLeft = Offset(dateX, plotH + 5f),
                        style = boldStyle,
                    )
                }

                // Static first / last anchors (dim)
                val firstLabel = shortLabel(labels.first())
                val lastLabel = shortLabel(labels.last())
                drawText(textMeasurer, firstLabel, topLeft = Offset(L_PAD, plotH + 5f), style = axisStyle)
                val lastMeasured = textMeasurer.measure(lastLabel, style = axisStyle)
                drawText(
                    textMeasurer, lastLabel,
                    topLeft = Offset(size.width - R_PAD - lastMeasured.size.width, plotH + 5f),
                    style = axisStyle,
                )
            }
        }
    }
}

private fun shortLabel(label: String): String =
    if (label.length >= 10 && label[4] == '-' && label[7] == '-') label.substring(5) else label.take(8)
