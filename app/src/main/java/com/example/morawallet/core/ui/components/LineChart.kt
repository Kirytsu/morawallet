package com.example.morawallet.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Minimal line chart drawn directly on a Canvas.
 *
 * @param showEndpoint draws a filled marker on the latest value.
 * @param gridColor when non-null, draws a faint baseline grid for readability.
 */
@Composable
fun LineChart(
    values: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    showEndpoint: Boolean = false,
    gridColor: Color? = null,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas

        val maxValue = values.max()
        val minValue = values.min()
        val span = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }
        val width = size.width
        val height = size.height
        val stepX = width / (values.size - 1)

        fun yFor(value: Double): Float = (height - ((value - minValue) / span) * height).toFloat()

        gridColor?.let { gc ->
            val rows = 3
            val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
            for (i in 0..rows) {
                val y = height / rows * i
                drawLine(
                    color = gc,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1.5f,
                    pathEffect = dashed,
                )
            }
        }

        val line = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = yFor(value)
            if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }

        drawPath(path = line, color = lineColor, style = Stroke(width = 5f))

        if (showEndpoint) {
            val lastX = (values.size - 1) * stepX
            val lastY = yFor(values.last())
            drawCircle(color = Color.White, radius = 9f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
            drawCircle(color = lineColor, radius = 6f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
        }
    }
}
