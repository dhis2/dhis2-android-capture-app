package org.dhis2.composetable.model

import kotlinx.serialization.Serializable
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

    fun cellHasError(cell: TableSelection.CellSelection): TableCell? {
        return when {
            tableRows.size == 1 && tableRows.size == cell.rowIndex -> {
                tableRows[0].values[cell.columnIndex]?.takeIf { it.error != null }
            }
            tableRows.size == cell.rowIndex -> {
                tableRows[cell.rowIndex - 1].values[cell.columnIndex]?.takeIf { it.error != null }
            }
            else -> tableRows[cell.rowIndex].values[cell.columnIndex]?.takeIf { it.error != null }
        }
    }

    fun hasCellWithId(cellId: String?): Boolean {
        return tableRows.any { row ->
            row.rowHeader.id?.let {
                it.isNotEmpty() && cellId?.contains(it) == true
            } ?: false
        }
    }
}
