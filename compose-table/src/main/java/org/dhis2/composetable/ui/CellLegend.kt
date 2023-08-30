package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CellLegendBox(
    modifier: Modifier = Modifier,
    legendColor: Color?,
    content: @Composable
    BoxScope.() -> Unit,
) {
    val boxModifier = legendColor?.let {
        val cornerSize = LocalTableDimensions.current.defaultLegendCornerSize
        val borderWidth = LocalTableDimensions.current.defaultLegendBorderWidth
        modifier
            .clip(shape = RoundedCornerShape(size = cornerSize))
            .drawBehind {
                drawRect(
                    color = legendColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(borderWidth.toPx(), size.height),
                )
            }
            .background(color = legendColor.copy(alpha = 0.15f))
    } ?: modifier
    Box(
        modifier = boxModifier,
        content = content,
    )
}

@Composable
@Preview
fun CellLegendPreview() {
    CellLegendBox(
        modifier = Modifier
            .width(44.dp)
            .height(16.dp),
        legendColor = Color(44, 152, 240),
    ) {}
}
