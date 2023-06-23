package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.extensions.areAllValuesEmpty
import org.dhis2.composetable.ui.compositions.LocalInteraction
import org.dhis2.composetable.ui.compositions.LocalTableResizeActions

@Composable
fun DataTable(tableList: List<TableModel>, bottomContent: @Composable (() -> Unit)? = null) {
    if (!TableTheme.configuration.editable && !tableList.all { it.areAllValuesEmpty() }) {
        val tableInteractions = LocalInteraction.current
        val tableResizeActions = LocalTableResizeActions.current
        TableItem(
            tableModel = tableList.first(),
            tableInteractions = tableInteractions,
            onSizeChanged = {
                tableResizeActions.onTableWidthChanged(it.width)
            },
            onColumnResize = { column, width ->
                tableResizeActions.onColumnHeaderResize(
                    tableList.first().id ?: "",
                    column,
                    width
                )
            },
            onHeaderResize = { width ->
                tableResizeActions.onRowHeaderResize(
                    tableList.first().id ?: "",
                    width
                )
            },
            onTableResize = { newValue ->
                tableResizeActions.onTableDimensionResize(
                    tableList.first().id ?: "",
                    newValue
                )
            },
            onResetResize = {
                tableResizeActions.onTableDimensionReset(tableList.first().id ?: "")
            }
        )
    } else if (TableTheme.configuration.editable) {
        TableList(
            tableList = tableList,
            bottomContent = bottomContent
        )
    }
}
