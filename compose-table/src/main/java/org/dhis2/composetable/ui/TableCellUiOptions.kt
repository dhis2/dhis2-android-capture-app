package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dhis2.composetable.model.TableCell

class TableCellUiOptions(
    private val cellValue: TableCell,
    val selectionState: SelectionState
) {

    @Composable
    fun backgroundColor() = when {
        cellValue.legendColor != null -> Color(cellValue.legendColor)
        cellValue.editable == false -> TableTheme.colors.disabledCellBackground
        else -> selectionState.colorForCell(column = cellValue.column, row = cellValue.row)
    }

    @Composable
    fun borderColor() = when {
        cellValue.isSelected(selectionState) -> when {
            cellValue.error != null -> TableTheme.colors.errorColor
            else -> TableTheme.colors.primary
        }
        else -> Color.Transparent
    }
}
