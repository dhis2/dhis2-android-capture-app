package org.dhis2.composetable.model

import androidx.compose.ui.geometry.Offset

data class ResizingCell(
    val initialPosition: Offset,
    val draggingOffsetX: Float,
)
