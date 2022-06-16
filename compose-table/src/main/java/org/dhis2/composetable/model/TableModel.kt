package org.dhis2.composetable.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TableModel(
    val tableHeaderModel: TableHeader,
    val tableRows: List<TableRowModel>
)

data class TableHeader(val rows: List<TableHeaderRow>, val hasTotals: Boolean = false) {
    val defaultCellWidth = 100.dp
    val defaultCellHeight = 56.dp
    fun numberOfColumns(rowIndex: Int): Int {
        var totalCells = 1
        for (index in 0 until rowIndex + 1) {
            totalCells *= rows[index].cells.size
        }
        return totalCells
    }

    fun tableMaxColumns() = numberOfColumns(rows.size - 1)

    fun cellWidth(rowIndex: Int): Dp {
        val fullWidth = defaultCellWidth * tableMaxColumns()
        return fullWidth / numberOfColumns(rowIndex)
    }
}

data class TableHeaderRow(val cells: List<TableHeaderCell>)
data class TableHeaderCell(val value: String)

data class TableRowModel(val rowHeader: String, val values: Map<Int, TableHeaderCell>) {
    val defaultWidth = 100.dp
}
