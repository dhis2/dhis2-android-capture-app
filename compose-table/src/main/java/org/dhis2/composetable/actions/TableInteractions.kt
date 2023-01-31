package org.dhis2.composetable.actions

import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.ui.TableSelection

interface TableInteractions {
    fun onSelectionChange(newTableSelection: TableSelection) = run { }
    fun onDecorationClick(dialogModel: TableDialogModel) = run { }
    fun onClick(tableCell: TableCell) = run { }
    fun onRowHeaderSizeChanged(tableId: String, widthDpValue: Float) = run { }
    fun onColumnHeaderSizeChanged(tableId: String, column: Int, widthDpValue: Float) = run { }
    fun onOptionSelected(cell: TableCell, code: String, label: String) = run { }
}
