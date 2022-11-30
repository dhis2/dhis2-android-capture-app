package org.dhis2.composetable.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp

data class ResizingCell(
    val cellWidth: Dp,
    val initialPosition: Offset,
    val draggingOffsetX: Float
)
