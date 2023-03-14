package org.dhis2.composetable.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class TableConfiguration(
    val headerActionsEnabled: Boolean = true,
    val editable: Boolean = true,
    val textInputViewMode: Boolean = true
) {
    fun shouldHideInput(
        keyboardIsClosed: Boolean,
        inputIsShown: Boolean
    ): Boolean = !textInputViewMode &&
        keyboardIsClosed &&
        inputIsShown
}

val LocalTableConfiguration = staticCompositionLocalOf { TableConfiguration() }
