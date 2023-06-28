package org.dhis2.composetable.actions

import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.ui.TableSelection

interface TableInteractions {
    fun onSelectionChange(newTableSelection: TableSelection) = run { }
    fun onDecorationClick(dialogModel: TableDialogModel) = run { }
    fun onClick(tableCell: TableCell) = run { }
    fun onTableSizeChanged(width: Int) = run {}
    fun onRowHeaderSizeChanged(tableId: String, newValue: Float) = run { }
    fun onColumnHeaderSizeChanged(tableId: String, column: Int, newValue: Float) = run { }
    fun onOptionSelected(cell: TableCell, code: String, label: String) = run { }
    fun onTableWidthReset(tableId: String) = run { }
    fun onTableWidthChanged(tableId: String, newValue: Float) = run { }
}
