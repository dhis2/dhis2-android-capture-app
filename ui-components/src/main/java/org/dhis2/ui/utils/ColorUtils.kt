package org.dhis2.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import org.dhis2.ui.theme.contrastDark
import org.dhis2.ui.theme.contrastLight
import kotlin.math.pow

fun Int.getAlphaContrastColor(): Color {
    val rgb = listOf(
        red / 255.0,
        green / 255.0,
        blue / 255.0,
    ).map {
        when {
            it <= 0.03928 -> it / 12.92
            else -> ((it + 0.055) / 1.055).pow(2.4)
        }
    }
    val l = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
    return when {
        l > 0.500 -> contrastDark
        else -> contrastLight
    }
}
