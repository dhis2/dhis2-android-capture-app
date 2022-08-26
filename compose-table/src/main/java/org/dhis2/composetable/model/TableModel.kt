package org.dhis2.composetable.model

import androidx.compose.ui.unit.Dp
import kotlinx.serialization.Serializable
import org.dhis2.composetable.ui.SelectionState
import org.dhis2.composetable.ui.TableSelection

@Serializable
data class TableModel(
    val id: String? = null,
    val tableHeaderModel: TableHeader,
    val tableRows: List<TableRowModel>,
    val upperPadding: Boolean = true,
    val overwrittenValues: Map<Int, TableCell> = emptyMap()
) {
    fun countChildrenOfSelectedHeader(headerRowIndex: Int): Int? {
        return tableHeaderModel.rows
            .filterIndexed { index, _ -> index > headerRowIndex }
            .map { row -> row.cells.size }
            .reduceOrNull { acc, i -> acc * i }
    }

    fun getNextCell(
        cellSelection: TableSelection.CellSelection
    ): Pair<TableCell, TableSelection.CellSelection>? = when {
        tableRows[cellSelection.rowIndex].values[cellSelection.columnIndex]?.error != null ->
            cellSelection
        cellSelection.columnIndex < tableHeaderModel.tableMaxColumns() - 1 ->
            cellSelection.copy(columnIndex = cellSelection.columnIndex + 1)
        cellSelection.rowIndex < tableRows.size - 1 ->
            cellSelection.copy(columnIndex = 0, rowIndex = cellSelection.rowIndex + 1)
        else -> null
    }?.let { nextCell ->
        val tableCell = tableRows[nextCell.rowIndex].values[nextCell.columnIndex]
        when (tableCell?.editable) {
            true -> Pair(tableCell, nextCell)
            else -> null
        }
    }
}

@Serializable
data class TableHeader(val rows: List<TableHeaderRow>, val hasTotals: Boolean = false) {
    fun numberOfColumns(rowIndex: Int): Int {
        var totalCells = 1
        for (index in 0 until rowIndex + 1) {
            totalCells *= rows[index].cells.size
        }
        return totalCells
    }

    fun tableMaxColumns() = numberOfColumns(rows.size - 1)
}

@Serializable
data class TableHeaderRow(val cells: List<TableHeaderCell>)

@Serializable
data class TableHeaderCell(val value: String)

@Serializable
data class TableCell(
    val id: String? = null,
    val row: Int? = null,
    val column: Int? = null,
    val value: String?,
    val editable: Boolean = true,
    val mandatory: Boolean? = false,
    val error: String? = null,
    val dropDownOptions: List<String>? = null,
    val legendColor: Int? = null
) {
    fun isSelected(selectionState: SelectionState): Boolean {
        return selectionState.cellOnly &&
            selectionState.row == row &&
            selectionState.column == column
    }
}

@Serializable
data class TableRowModel(
    val rowHeader: RowHeader,
    val values: Map<Int, TableCell>,
    val isLastRow: Boolean = false
)

@Serializable
data class RowHeader(
    val id: String?,
    val title: String,
    val row: Int? = null,
    val showDecoration: Boolean = false,
    val description: String? = null
)

data class HeaderMeasures(val width: Dp, val height: Dp)
