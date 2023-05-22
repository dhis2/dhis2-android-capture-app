package org.dhis2.composetable.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TableColors(
    val primary: Color = Color(0xFF2C98F0),
    val primaryLight: Color = Color(0x332C98F0),
    val headerText: Color = Color(0x8A000000),
    val headerBackground1: Color = Color(0x05000000),
    val headerBackground2: Color = Color(0x0A000000),
    val cellText: Color = Color(0xDE000000),
    val disabledCellText: Color = Color(0x61000000),
    val disabledCellBackground: Color = Color(0x0A000000),
    val errorColor: Color = Color(0xFFE91E63),
    val warningColor: Color = Color(0xFFFF9800),
    val tableBackground: Color = Color(0xFFFFFFFF),
    val iconColor: Color = Color.LightGray
) {
    fun cellTextColor(hasError: Boolean, hasWarning: Boolean, isEditable: Boolean) = when {
        hasError -> errorColor
        hasWarning -> warningColor
        !isEditable -> disabledCellText
        else -> cellText
    }

    fun cellMandatoryIconColor(hasValue: Boolean) = when (hasValue) {
        true -> iconColor
        false -> errorColor
    }
}

val LocalTableColors = staticCompositionLocalOf { TableColors() }
