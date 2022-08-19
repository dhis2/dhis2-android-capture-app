package org.dhis2.composetable

import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.ui.TableSelection
import org.junit.Test

class TableModelTest {

    private val tableModel = TableModel(
        id = "table",
        tableHeaderModel = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableHeaderCell("1"),
                        TableHeaderCell("2"),
                        TableHeaderCell("3")
                    )
                )
            )
        ),
        tableRows = listOf(
            TableRowModel(
                rowHeader = RowHeader(
                    id = "0",
                    title = "Row 1",
                    row = 0
                ),
                values = mapOf(
                    Pair(0, TableCell("00", 0, 0, "1")),
                    Pair(1, TableCell("01", 0, 1, "2")),
                    Pair(2, TableCell("02", 0, 2, "3"))
                ),
            ),
            TableRowModel(
                rowHeader = RowHeader(
                    id = "1",
                    title = "Row 2",
                    row = 1
                ),
                values = mapOf(
                    Pair(0, TableCell("10", 1, 0, "4")),
                    Pair(1, TableCell("11", 1, 1, "5", editable = false)),
                    Pair(2, TableCell("12", 1, 2, "6"))
                )
            ),
        ),
    )

    @Test
    fun moveToCellOnTheRight() {
        val currentSelection = TableSelection.CellSelection("table", 0, 0)
        tableModel.getNextCell(currentSelection)?.let { (tableCell, nextSelection) ->
            assert(tableCell.id == "01")
            assert(nextSelection.rowIndex == 0)
            assert(nextSelection.columnIndex == 1)
        }
    }

    @Test
    fun moveToCellOnTheNextRow() {
        val currentSelection = TableSelection.CellSelection("table", 3, 0)
        tableModel.getNextCell(currentSelection)?.let { (tableCell, nextSelection) ->
            assert(tableCell.id == "10")
            assert(nextSelection.rowIndex == 1)
            assert(nextSelection.columnIndex == 0)
        }
    }

    @Test
    fun returnNullWhenSelectionIsLastCell() {
        val currentSelection = TableSelection.CellSelection("table", 3, 1)
        assert(tableModel.getNextCell(currentSelection) == null)
    }

    @Test
    fun returnNullWhenNextCellIsNotEditable() {
        val currentSelection = TableSelection.CellSelection("table", 0, 1)
        assert(tableModel.getNextCell(currentSelection) == null)
    }
}
