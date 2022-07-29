package org.dhis2.composetable.ui

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
        val childrenOfSelectedHeader: Int?
    ) : TableSelection(tableId)

    data class CellSelection(
        override val tableId: String,
        val columnIndex: Int,
        val rowIndex: Int
    ) : TableSelection(tableId)

    fun isCornerSelected(selectedTableId: String) =
        selectedTableId == tableId && (this is AllCellSelection)

    fun isHeaderSelected(
        selectedTableId: String,
        columnIndex: Int,
        columnHeaderRowIndex: Int
    ) = this.isCornerSelected(selectedTableId) ||
            selectedTableId == tableId && (this is ColumnSelection) &&
            this.columnIndex == columnIndex &&
            this.columnHeaderRow == columnHeaderRowIndex

    fun isParentHeaderSelected(
        selectedTableId: String,
        columnIndex: Int,
        columnHeaderRowIndex: Int
    ) = selectedTableId == tableId && (this is ColumnSelection) &&
            (this.childrenOfSelectedHeader?.let { columnIndex / it == this.columnIndex } ?: false ||
                    this.columnHeaderRow == columnHeaderRowIndex &&
                    this.columnIndex != columnIndex
                    )

    fun isRowSelected(
        selectedTableId: String,
        rowHeaderIndex: Int
    ) = this.isCornerSelected(selectedTableId) ||
            selectedTableId == tableId && (this is RowSelection) &&
            this.rowIndex == rowHeaderIndex

    fun isOtherRowSelected(
        selectedTableId: String,
        rowHeaderIndex: Int
    ) = selectedTableId == tableId && (this is RowSelection) &&
            this.rowIndex != rowHeaderIndex

    fun isCellSelected(
        selectedTableId: String,
        columnIndex: Int,
        rowIndex: Int
    ) = this.tableId == selectedTableId && (this is CellSelection) &&
            this.columnIndex == columnIndex &&
            this.rowIndex == rowIndex

    fun isCellParentSelected(
        selectedTableId: String,
        columnIndex: Int,
        rowIndex: Int
    ) = when (this) {
        is AllCellSelection -> isCornerSelected(selectedTableId)
        is ColumnSelection -> this.columnIndex == columnIndex &&
                childrenOfSelectedHeader == null ||
                this.childrenOfSelectedHeader?.let { columnIndex / it == this.columnIndex } ?: false
        is RowSelection -> isRowSelected(selectedTableId, rowIndex)
        else -> false
    }
}