package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import org.dhis2.composetable.model.TableCell

class TableCellUiOptions(
    private val cellValue: TableCell,
    val value: String,
    val focused: Boolean,
    val selectionState: SelectionState
) {

    @Composable
    fun backgroundColor() = when (cellValue.editable) {
        false -> TableTheme.colors.disabledCellBackground
        else -> selectionState.colorForCell(column = cellValue.column, row = cellValue.row)
    }

    @Composable
    fun textColor() = when {
        cellValue.error != null -> TableTheme.colors.errorColor
        cellValue.editable == false -> TableTheme.colors.disabledCellText
        else -> TableTheme.colors.cellText
    }

    @Composable
    fun mandatoryColor() = when {
        value.isNotEmpty() -> Color.LightGray
        else -> TableTheme.colors.errorColor
    }
    @Composable
    fun mandatoryAlignment() = when {
        value.isNotEmpty() -> Alignment.TopStart
        else -> Alignment.CenterEnd
    }

    val enabled = cellValue.editable == true

    @Composable
    fun borderColor() = when {
        focused -> when {
            cellValue.error != null -> TableTheme.colors.errorColor
            else -> TableTheme.colors.primary
        }
        else -> Color.Transparent
    }
}
