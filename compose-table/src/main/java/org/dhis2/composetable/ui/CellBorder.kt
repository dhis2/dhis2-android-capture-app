package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.cellBorder(borderWidth: Dp = 1.dp, borderColor: Color, backgroundColor: Color) =
    this.then(
        border(borderWidth, borderColor)
            .background(backgroundColor)
    )
