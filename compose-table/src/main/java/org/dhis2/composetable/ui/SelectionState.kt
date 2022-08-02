package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Stable
class SelectionState {

    var column: Int? by mutableStateOf(null)
    var row: Int? by mutableStateOf(null)
    var all: Boolean by mutableStateOf(false)
    var rowHeader: Boolean by mutableStateOf(false)
    var columnHeaderRow: Int? by mutableStateOf(null)
    var sonsOfHeader: Int? by mutableStateOf(null)
    var cellOnly: Boolean by mutableStateOf(false)

    fun selectCell(
        column: Int? = null,
        row: Int? = null,
        all: Boolean = false,
        rowHeader: Boolean = false,
        columnHeaderRow: Int? = null,
        cellOnly: Boolean = false
    ) {
        this.column = column
        this.row = row
        this.all = all
        this.rowHeader = rowHeader
        this.columnHeaderRow = columnHeaderRow
        this.cellOnly = cellOnly
        this.sonsOfHeader = null
    }

    fun selectHeader(
        column: Int? = null,
        row: Int? = null,
        childrenOfSelectedHeader: Int? = null,
        all: Boolean = false,
        rowHeader: Boolean = false,
        columnHeaderRow: Int? = null,
        cellOnly: Boolean = false
    ) {
        this.column = column
        this.row = row
        this.all = all
        this.rowHeader = rowHeader
        this.columnHeaderRow = columnHeaderRow
        this.cellOnly = cellOnly
        this.sonsOfHeader = childrenOfSelectedHeader
    }

    private fun selectedHeaderIsParent(columnIndex: Int): Boolean {
        return sonsOfHeader?.let { columnIndex / it == column } ?: false
    }

    @Composable
    fun colorForCell(column: Int?, row: Int?): Color = when {
        cellOnly -> TableTheme.colors.tableBackground
        isParentSelection(column, row) -> TableTheme.colors.primaryLight
        else -> TableTheme.colors.tableBackground
    }

    fun isParentSelection(column: Int?, row: Int?): Boolean {
        return column != null && row != null && (
            all || column == this.column && sonsOfHeader == null ||
                row == this.row || selectedHeaderIsParent(column)
            )
    }
}

@Composable
fun rememberSelectionState(): SelectionState {
    return remember { SelectionState() }
}
