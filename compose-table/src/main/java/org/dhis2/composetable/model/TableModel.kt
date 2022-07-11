package org.dhis2.composetable.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TableModel(
    val id: String? = null,
    val tableHeaderModel: TableHeader,
    val tableRows: List<TableRowModel>
)

data class TableHeader(val rows: List<TableHeaderRow>, val hasTotals: Boolean = false) {
    val defaultCellWidth = 52.dp
    val defaultHeaderHeight = 24.dp
    fun numberOfColumns(rowIndex: Int): Int {
        var totalCells = 1
        for (index in 0 until rowIndex + 1) {
            totalCells *= rows[index].cells.size
        }
        return totalCells
    }

    fun tableMaxColumns() = numberOfColumns(rows.size - 1)

    fun headerCellWidth(rowIndex: Int): Dp {
        val fullWidth = defaultCellWidth * tableMaxColumns()
        return fullWidth / numberOfColumns(rowIndex)
    }
}

data class TableHeaderRow(val cells: List<TableHeaderCell>)
data class TableHeaderCell(val value: String)
data class TableCell(
    val id: String? = null,
    val row: Int? = null,
    val column: Int? = null,
    val value: String?,
    val editable: Boolean? = true,
    val mandatory: Boolean? = false,
    val error: String? = null,
    val isReadOnly: Boolean = false
)

data class TableRowModel(val rowHeader: RowHeader, val values: Map<Int, TableCell>)
data class RowHeader(val title: String, val row: Int? = null, val showDecoration: Boolean = false) {
    val defaultCellHeight = 36.dp
    val defaultWidth = 60.dp
}
