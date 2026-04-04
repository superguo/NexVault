package com.nexvault.wallet.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nexvault.wallet.domain.model.token.PricePoint

/**
 * Minimal line chart for price or portfolio history.
 *
 * @param dataPoints At least two points are required; otherwise nothing is drawn.
 * @param lineColor Stroke color for the line.
 */
@Composable
fun SimpleLineChart(
    dataPoints: List<PricePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color,
) {
    if (dataPoints.size < 2) return

    val minValue = dataPoints.minOf { it.value }
    val maxValue = dataPoints.maxOf { it.value }
    val valueRange = (maxValue - minValue).coerceAtLeast(0.01)

    Canvas(modifier = modifier.padding(8.dp)) {
        val stepX = size.width / (dataPoints.size - 1)
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / valueRange * size.height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
