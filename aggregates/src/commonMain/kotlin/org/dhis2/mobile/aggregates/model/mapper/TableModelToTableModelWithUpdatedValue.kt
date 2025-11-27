package org.dhis2.mobile.aggregates.model.mapper

import androidx.compose.ui.graphics.toArgb
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalHeaderRowId
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCellContent
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

internal suspend fun TableModel.updateValue(
    cellId: String?,
    updatedValue: String?,
    legendData: LegendData?,
    error: String?,
    resourceManager: ResourceManager,
): TableModel {
    CoroutineTracker.increment()
    val hasTotalColumn = tableHeaderModel.extraColumns.isNotEmpty()
    val hasTotalRow = tableRows.last().id() == totalHeaderRowId(id)
    val tableRows =
        tableRows.map { tableRowModel ->
            val cell =
                tableRowModel.values.values.find { tableCell ->
                    tableCell.id == cellId
                }
            val totalsColumnCell =
                tableRowModel.values.values
                    .lastOrNull()
                    .takeIf { hasTotalColumn }
            if (cell != null) {
                val updatedValues = tableRowModel.values.toMutableMap()
                updatedValues[cell.column] =
                    cell.copy(
                        content =
                            when (cell.content) {
                                is TableCellContent.Text -> TableCellContent.Text(updatedValue)
                                is TableCellContent.Checkbox ->
                                    TableCellContent.Checkbox(
                                        isChecked = updatedValue?.toBoolean() ?: false,
                                    )
                            },
                        error = error,
                        legendColor = legendData?.color?.toArgb(),
                    )
                totalsColumnCell?.let { totalCell ->
                    val totalValue =
                        updatedValues.values
                            .toList()
                            .dropLast(1)
                            .sumOf { tableCell ->
                                tableCell.value?.toDoubleOrNull() ?: 0.0
                            }

                    updatedValues[tableRowModel.values.size - 1] =
                        totalCell.copy(content = TableCellContent.Text(totalValue.toString()))
                }
                tableRowModel.copy(values = updatedValues)
            } else {
                tableRowModel
            }
        }

    return if (hasTotalRow) {
        CoroutineTracker.decrement()
        val totalRowIndex = tableRows.last().row()
        copy(tableRows = tableRows.dropLast(1)).withTotalsRow(resourceManager, totalRowIndex)
    } else {
        CoroutineTracker.decrement()
        copy(tableRows = tableRows)
    }
}
