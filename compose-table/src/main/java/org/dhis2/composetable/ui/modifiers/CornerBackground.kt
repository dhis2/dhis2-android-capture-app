package org.dhis2.composetable.ui.modifiers

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

fun Modifier.cornerBackground(isSelected: Boolean, selectedColor: Color, defaultColor: Color) =
    this.then(
        background(
            color = if (isSelected) {
                selectedColor
            } else {
                defaultColor
            },
        ),
    )
