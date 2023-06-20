package org.dhis2.composetable.actions

import androidx.compose.runtime.compositionLocalOf
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.ui.TableSelection

interface TableInteractions {
    fun onSelectionChange(newTableSelection: TableSelection) = run { }
    fun onDecorationClick(dialogModel: TableDialogModel) = run { }
    fun onClick(tableCell: TableCell) = run { }
    fun onOptionSelected(cell: TableCell, code: String, label: String) = run { }
}

val LocalInteraction = compositionLocalOf<TableInteractions> { object : TableInteractions {} }
