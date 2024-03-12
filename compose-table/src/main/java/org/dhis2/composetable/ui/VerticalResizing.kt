package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.R
import org.dhis2.composetable.model.ResizingCell
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun VerticalResizingView(modifier: Modifier = Modifier, provideResizingCell: () -> ResizingCell?) {
    val colorPrimary = TableTheme.colors.primary
    provideResizingCell()?.let { resizingCell ->
        val offsetX = resizingCell.initialPosition.x + resizingCell.draggingOffsetX
        Box(
            modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .fillMaxHeight()
                .drawBehind {
                    drawRect(
                        color = colorPrimary,
                        topLeft = Offset(0f, 0f),
                        size = Size(2.dp.toPx(), size.height),
                    )
                }
                .graphicsLayer(clip = false),
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(
                            -15.dp.value.toInt(),
                            resizingCell.initialPosition.y.roundToInt(),
                        )
                    }
                    .background(
                        color = colorPrimary,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .size(14.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_row_widener),
                contentDescription = "",
                tint = Color.White,
            )
        }
    }
}

@Composable
fun VerticalResizingRule(
    modifier: Modifier = Modifier,
    checkMaxMinCondition: (dimensions: TableDimensions, currentOffsetX: Float) -> Boolean,
    onHeaderResize: (Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit,
) {
    var dimensions by remember { mutableStateOf<TableDimensions?>(null) }
    dimensions = TableTheme.dimensions

    val minOffset = with(LocalDensity.current) { 5.dp.toPx() }
    var offsetX by remember { mutableStateOf(minOffset) }
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .fillMaxHeight()
            .width(48.dp)
            .offset(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > minOffset) {
                            onHeaderResize(offsetX)
                        }
                        offsetX = minOffset
                        onResizing(null)
                    },
                ) { change, dragAmount ->
                    change.consume()
                    if (checkMaxMinCondition(dimensions!!, offsetX + dragAmount.x)) {
                        offsetX += dragAmount.x
                    }
                    onResizing(ResizingCell(positionInRoot, offsetX))
                }
            },
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = TableTheme.colors.primary,
                    shape = RoundedCornerShape(16.dp),
                )
                .size(14.dp)
                .onGloballyPositioned { coordinates ->
                    positionInRoot = coordinates.positionInRoot()
                },
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_row_widener),
            contentDescription = "",
            tint = Color.White,
        )
    }
}
