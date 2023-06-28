package org.dhis2.composetable.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TableSelectionTest {

    private val selectedTableId = "selectedTableId"
    private val selectedRow = 2
    private val selectedColumn = 3
    private val selectedColumnHeaderRow = 2
    private val childrenOfSelectedHeader = mapOf(
        selectedColumnHeaderRow to TableSelection.HeaderCellRange(3, 3, 5)
    )
    private val selectedChildColumn = 9

    private val otherTableId = "otherTableId"
    private val otherSelectedRow = 0
    private val otherSelectedColumn = 2
    private val otherSelectedColumnHeaderRow = 1

    val unselected = TableSelection.Unselected()
    val cornerSelection = TableSelection.AllCellSelection(selectedTableId)
    val rowSelection = TableSelection.RowSelection(
        selectedTableId,
        selectedRow
    )
    val columnSelection = TableSelection.ColumnSelection(
        selectedTableId,
        selectedColumn,
        selectedColumnHeaderRow,
        childrenOfSelectedHeader
    )
    val cellSelection = TableSelection.CellSelection(
        selectedTableId,
        selectedColumn,
        selectedRow,
        selectedRow
    )

    @Test
    fun `should return if corner is selected`() {
        assertTrue(cornerSelection.isCornerSelected(selectedTableId))
        assertFalse(cornerSelection.isCornerSelected(otherTableId))
        assertFalse(unselected.isCornerSelected(selectedTableId))
        assertFalse(rowSelection.isCornerSelected(selectedTableId))
        assertFalse(columnSelection.isCornerSelected(selectedTableId))
        assertFalse(cellSelection.isCornerSelected(selectedTableId))
    }

    @Test
    fun `should return if header is selected`() {
        assertTrue(
            cornerSelection.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            cornerSelection.isHeaderSelected(
                otherTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            unselected.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            rowSelection.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertTrue(
            columnSelection.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            columnSelection.isHeaderSelected(
                selectedTableId,
                otherSelectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            columnSelection.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                otherSelectedColumnHeaderRow
            )
        )
        assertFalse(
            cellSelection.isHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
    }

    @Test
    fun `should return if parent header is selected`() {
        assertFalse(
            cornerSelection.isParentHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            unselected.isParentHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            rowSelection.isParentHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            cellSelection.isParentHeaderSelected(
                selectedTableId,
                selectedColumn,
                selectedColumnHeaderRow
            )
        )

        assertTrue(
            columnSelection.isParentHeaderSelected(
                selectedTableId,
                otherSelectedColumn,
                selectedColumnHeaderRow
            )
        )
        assertFalse(
            columnSelection.isParentHeaderSelected(
                selectedTableId,
                selectedChildColumn,
                otherSelectedColumnHeaderRow
            )
        )
        assertFalse(
            columnSelection.isParentHeaderSelected(
                selectedTableId,
                selectedColumn,
                otherSelectedColumnHeaderRow
            )
        )
        assertFalse(
            columnSelection.isParentHeaderSelected(
                selectedTableId,
                otherSelectedRow,
                otherSelectedColumnHeaderRow
            )
        )
    }

    @Test
    fun `should return if row is selected`() {
        assertTrue(cornerSelection.isRowSelected(selectedTableId, selectedRow))
        assertFalse(unselected.isRowSelected(selectedTableId, selectedRow))
        assertFalse(columnSelection.isRowSelected(selectedTableId, selectedRow))
        assertFalse(cellSelection.isRowSelected(selectedTableId, selectedRow))

        assertTrue(rowSelection.isRowSelected(selectedTableId, selectedRow))
        assertFalse(rowSelection.isRowSelected(otherTableId, selectedRow))
        assertFalse(rowSelection.isRowSelected(selectedTableId, otherSelectedRow))
        assertTrue(rowSelection.isOtherRowSelected(selectedTableId, otherSelectedRow))
        assertFalse(rowSelection.isOtherRowSelected(selectedTableId, selectedRow))
        assertFalse(rowSelection.isOtherRowSelected(otherTableId, otherSelectedRow))
    }

    @Test
    fun `should return if cell is selected`() {
        assertFalse(cornerSelection.isCellSelected(selectedTableId, selectedColumn, selectedRow))
        assertFalse(unselected.isCellSelected(selectedTableId, selectedColumn, selectedRow))
        assertFalse(columnSelection.isCellSelected(selectedTableId, selectedColumn, selectedRow))
        assertFalse(rowSelection.isCellSelected(selectedTableId, selectedColumn, selectedRow))

        assertTrue(cellSelection.isCellSelected(selectedTableId, selectedColumn, selectedRow))
        assertFalse(cellSelection.isCellSelected(otherTableId, selectedColumn, selectedRow))
        assertFalse(cellSelection.isCellSelected(selectedTableId, otherSelectedColumn, selectedRow))
        assertFalse(cellSelection.isCellSelected(selectedTableId, selectedColumn, otherSelectedRow))
    }

    @Test
    fun `should return if parent cell is selected`() {
        assertTrue(
            cornerSelection.isCellParentSelected(
                selectedTableId,
                selectedColumn,
                selectedRow
            )
        )
        assertFalse(unselected.isCellParentSelected(selectedTableId, selectedColumn, selectedRow))

        assertTrue(
            columnSelection.isCellParentSelected(
                selectedTableId,
                selectedColumn,
                selectedRow
            )
        )
        assertFalse(
            columnSelection.isCellParentSelected(
                selectedTableId,
                otherSelectedColumn,
                selectedRow
            )
        )
        assertFalse(
            columnSelection.isCellParentSelected(
                otherTableId,
                selectedColumn,
                selectedRow
            )
        )

        assertTrue(
            rowSelection.isCellParentSelected(selectedTableId, selectedColumn, selectedRow)
        )

        assertFalse(
            cellSelection.isCellParentSelected(
                selectedTableId,
                selectedColumn,
                selectedRow
            )
        )
    }

    @Test
    fun `Should return selected cell row index`() {
        assertTrue(cellSelection.getSelectedCellRowIndex(selectedTableId) == selectedRow)
        assertTrue(cellSelection.getSelectedCellRowIndex(selectedTableId) != 1)
        assertTrue(cellSelection.getSelectedCellRowIndex("other table") == -1)
    }
}
