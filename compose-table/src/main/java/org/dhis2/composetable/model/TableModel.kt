package org.dhis2.composetable.model

import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.Serializable
import org.dhis2.composetable.ui.SelectionState
import org.dhis2.composetable.ui.TableSelection

@Serializable
data class TableModel(
    val id: String? = null,
    val title: String = "",
    val tableHeaderModel: TableHeader,
    val tableRows: List<TableRowModel>,
    val overwrittenValues: Map<Int, TableCell> = emptyMap()
) {
    fun countChildrenOfSelectedHeader(
        headerRowIndex: Int,
        headerColumnIndex: Int
    ): Map<Int, TableSelection.HeaderCellRange> {
        return tableHeaderModel.rows
            .filterIndexed { index, _ -> index > headerRowIndex }
            .mapIndexed { index, _ ->
                val rowIndex = headerRowIndex + 1 + index
                val rowSize =
                    tableHeaderModel.numberOfColumns(rowIndex) / tableHeaderModel.numberOfColumns(
                        headerRowIndex
                    )
                val init = headerColumnIndex * rowSize
                val end = (headerColumnIndex + 1) * rowSize - 1
                rowIndex to TableSelection.HeaderCellRange(rowSize, init, end)
            }.toMap()
    }

    fun getNextCell(
        cellSelection: TableSelection.CellSelection,
        successValidation: Boolean
    ): Pair<TableCell, TableSelection.CellSelection>? = when {
        !successValidation ->
            cellSelection
        cellSelection.columnIndex < tableHeaderModel.tableMaxColumns() - 1 ->
            cellSelection.copy(columnIndex = cellSelection.columnIndex + 1)
        cellSelection.rowIndex < tableRows.size - 1 ->
            cellSelection.copy(
                columnIndex = 0,
                rowIndex = cellSelection.rowIndex + 1,
                globalIndex = cellSelection.globalIndex + 1
            )
        else -> null
    }?.let { nextCell ->
        val tableCell = tableRows[nextCell.rowIndex].values[nextCell.columnIndex]
        when (tableCell?.editable) {
            true -> Pair(tableCell, nextCell)
            else -> getNextCell(nextCell, successValidation)
        }
    }

    fun cellHasError(cell: TableSelection.CellSelection): TableCell? =
        tableRows[cell.rowIndex].values[cell.columnIndex]?.takeIf { it.error != null }

    fun hasCellWithId(cellId: String?): Boolean {
        return tableRows.any { row ->
            row.rowHeader.id?.let {
                it.isNotEmpty() && cellId?.contains(it) == true
            } ?: false
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

@Serializable
data class TableRowModel(
    val rowHeader: RowHeader,
    val values: Map<Int, TableCell>,
    val isLastRow: Boolean = false,
    val maxLines: Int = 3,
    val dropDownOptions: List<String>? = null
)

@Serializable
data class RowHeader(
    val id: String? = null,
    val title: String,
    val row: Int? = null,
    val showDecoration: Boolean = false,
    val description: String? = null
)

data class HeaderMeasures(val width: Int, val height: Int)

fun TableModel.areAllValuesEmpty(): Boolean {
    this.tableRows.forEach { it ->
        val result = it.values.values.filterNot { it.value == "" }
        if (result.isNotEmpty()) {
            return false
        }
    }
    return true
}

val LocalCurrentCellValue = compositionLocalOf<() -> String?> { { "" } }
val LocalUpdatingCell = compositionLocalOf<TableCell?> { null }
