package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ExtendDivider(tableId: String, selected: Boolean) {
    val background = TableTheme.colors.primary
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(
                    with(LocalDensity.current) {
                        TableTheme.dimensions
                            .rowHeaderWidth(tableId)
                            .toDp()
                    }
                )
                .height(8.dp)
                .background(
                    color = if (selected) {
                        TableTheme.colors.primary
                    } else {
                        Color.White
                    }
                )
                .drawBehind {
                    drawRect(
                        color = background,
                        topLeft = Offset(size.width - 1.dp.toPx(), 0f),
                        size = Size(1.dp.toPx(), size.height)
                    )
                }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = if (selected) {
                        TableTheme.colors.primaryLight
                    } else {
                        Color.White
                    }
                )
        )
    }
}
