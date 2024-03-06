package org.dhis2.composetable.model

import kotlinx.serialization.Serializable
import org.dhis2.composetable.ui.SelectionState

@Serializable
data class TableCell(
    val id: String? = null,
    val row: Int? = null,
    val column: Int? = null,
    val value: String?,
    val editable: Boolean = true,
    val mandatory: Boolean? = false,
    val error: String? = null,
    val warning: String? = null,
    val legendColor: Int? = null
) {
    fun isSelected(selectionState: SelectionState): Boolean {
        return selectionState.cellOnly &&
            selectionState.row == row &&
            selectionState.column == column
    }

    fun hasErrorOrWarning() = errorOrWarningMessage() != null
    fun errorOrWarningMessage() = error ?: warning
}
