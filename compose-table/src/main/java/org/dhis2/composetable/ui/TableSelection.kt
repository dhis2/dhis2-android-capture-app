package org.dhis2.composetable.ui

import androidx.compose.runtime.staticCompositionLocalOf

sealed class TableSelection(open val tableId: String) {
    data class Unselected(
        val previousSelectedTableId: String? = null
    ) : TableSelection(previousSelectedTableId ?: "")

    data class AllCellSelection(
        override val tableId: String
    ) : TableSelection(tableId)

    data class RowSelection(
        override val tableId: String,
        val rowIndex: Int
    ) : TableSelection(tableId)

    data class ColumnSelection(
        override val tableId: String,
        val columnIndex: Int,
        val columnHeaderRow: Int,
        val childrenOfSelectedHeader: Map<Int, HeaderCellRange>
    ) : TableSelection(tableId)

    data class HeaderCellRange(val size: Int, val firstIndex: Int, val lastIndex: Int) {
        fun isInRange(columnIndex: Int): Boolean {
            return columnIndex in firstIndex..lastIndex
        }
    }

    data class CellSelection(
        override val tableId: String,
        val columnIndex: Int,
        val rowIndex: Int,
        val globalIndex: Int
    ) : TableSelection(tableId)

    fun getSelectedCellRowIndex(selectedTableId: String): Int =
        if (selectedTableId == tableId && this is CellSelection) {
            this.rowIndex
        } else {
            -1
        }

    fun isCornerSelected(selectedTableId: String) =
        selectedTableId == tableId && (this is AllCellSelection)

    fun isHeaderSelected(selectedTableId: String, columnIndex: Int, columnHeaderRowIndex: Int) =
        this.isCornerSelected(selectedTableId) ||
            selectedTableId == tableId && (this is ColumnSelection) &&
            this.columnIndex == columnIndex &&
            this.columnHeaderRow == columnHeaderRowIndex

    fun isParentHeaderSelected(
        selectedTableId: String,
        columnIndex: Int,
        columnHeaderRowIndex: Int
    ) = selectedTableId == tableId && (this is ColumnSelection) &&
        (
            when {
                columnHeaderRowIndex < this.columnHeaderRow -> false
                columnHeaderRowIndex == this.columnHeaderRow -> this.columnIndex != columnIndex
                else -> this.childrenOfSelectedHeader[columnHeaderRowIndex]?.isInRange(
                    columnIndex
                ) ?: false
            }
            )

    fun isRowSelected(selectedTableId: String, rowHeaderIndex: Int) =
        this.isCornerSelected(selectedTableId) ||
            selectedTableId == tableId && (this is RowSelection) &&
            this.rowIndex == rowHeaderIndex

    fun isOtherRowSelected(selectedTableId: String, rowHeaderIndex: Int) =
        selectedTableId == tableId && (this is RowSelection) &&
            this.rowIndex != rowHeaderIndex

    fun isCellSelected(selectedTableId: String, columnIndex: Int, rowIndex: Int) =
        this.tableId == selectedTableId && (this is CellSelection) &&
            this.columnIndex == columnIndex &&
            this.rowIndex == rowIndex

    fun isCellParentSelected(selectedTableId: String, columnIndex: Int, rowIndex: Int) =
        when (this) {
            is AllCellSelection -> isCornerSelected(selectedTableId)
            is ColumnSelection ->
                if (childrenOfSelectedHeader.isEmpty()) {
                    isCellValid(columnIndex, rowIndex) &&
                        this.columnIndex == columnIndex &&
                        this.tableId == selectedTableId
                } else {
                    isCellValid(columnIndex, rowIndex) &&
                        this.tableId == selectedTableId &&
                        this.childrenOfSelectedHeader.values.last().isInRange(columnIndex)
                }
            is RowSelection -> {
                isRowSelected(selectedTableId, rowIndex)
            }
            else -> false
        }

    private fun isCellValid(columnIndex: Int, rowIndex: Int): Boolean {
        return columnIndex != -1 && rowIndex != -1
    }
}

val LocalTableSelection = staticCompositionLocalOf<TableSelection> { TableSelection.Unselected() }
