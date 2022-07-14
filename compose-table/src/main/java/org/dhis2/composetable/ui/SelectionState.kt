package org.dhis2.composetable.ui

import androidx.compose.material.contentColorFor
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
    }

    @Composable
    fun styleForColumnHeader(column: Int, headerRow: Int): CellStyle = when {
        all || headerRow == columnHeaderRow && column == this.column -> CellStyle(
            backgroundColor = TableTheme.colors.primary,
            textColor = contentColorFor(TableTheme.colors.primary)
        )
        selectedHeaderIsParent(column) ||
                headerRow == columnHeaderRow && column != this.column -> CellStyle(
            backgroundColor = TableTheme.colors.primaryLight,
            textColor = TableTheme.colors.headerText
        )
        column % 2 == 0 -> CellStyle(
            backgroundColor = TableTheme.colors.headerBackground1,
            textColor = TableTheme.colors.headerText
        )
        else ->
            CellStyle(
                backgroundColor = TableTheme.colors.headerBackground2,
                textColor = TableTheme.colors.headerText
            )
    }

    @Composable
    fun styleForRowHeader(rowIndex: Int?): CellStyle = when {
        rowIndex == row && rowHeader || all -> CellStyle(
            TableTheme.colors.primary,
            contentColorFor(TableTheme.colors.primary)
        )
        rowIndex != row && rowHeader -> CellStyle(
            TableTheme.colors.primaryLight,
            TableTheme.colors.primary
        )
        else -> CellStyle(
            backgroundColor = TableTheme.colors.tableBackground,
            textColor = TableTheme.colors.primary
        )
    }

    private fun selectedHeaderIsParent(columnIndex: Int): Boolean {
        return sonsOfHeader?.let { columnIndex / it == column } ?: false
    }

    @Composable
    fun backgroundColorForCorner(): Color = when (all) {
        true -> TableTheme.colors.primaryLight
        else -> TableTheme.colors.tableBackground
    }

    @Composable
    fun colorForCell(column: Int?, row: Int?): Color = when {
        cellOnly -> TableTheme.colors.tableBackground
        column != null && row != null && (
                all || column == this.column && sonsOfHeader == null ||
                        row == this.row || selectedHeaderIsParent(column)
                ) -> TableTheme.colors.primaryLight
        else -> TableTheme.colors.tableBackground
    }
}

@Composable
fun rememberSelectionState(): SelectionState {
    return remember { SelectionState() }
}
