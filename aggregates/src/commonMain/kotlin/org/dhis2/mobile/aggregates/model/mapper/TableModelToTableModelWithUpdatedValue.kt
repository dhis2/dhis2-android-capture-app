package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalHeaderRowId
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

internal suspend fun TableModel.updateValue(
    cellId: String?,
    updatedValue: String?,
    resourceManager: ResourceManager,
): TableModel {
    val hasTotalColumn = tableHeaderModel.extraColumns.isNotEmpty()
    val hasTotalRow = tableRows.last().id() == totalHeaderRowId(id)
    val tableRows = tableRows.map { tableRowModel ->
        val cell = tableRowModel.values.values.find { tableCell ->
            tableCell.id == cellId
        }
        val totalsColumnCell =
            tableRowModel.values.values.last().takeIf { hasTotalColumn }
        if (cell != null) {
            val updatedValues = tableRowModel.values.toMutableMap()
            updatedValues[cell.column] = cell.copy(value = updatedValue)
            totalsColumnCell?.let { totalCell ->
                val totalValue = updatedValues.values.toList().dropLast(1)
                    .sumOf { tableCell ->
                        tableCell.value?.toDoubleOrNull() ?: 0.0
                    }

                updatedValues[tableRowModel.values.size - 1] =
                    totalCell.copy(value = totalValue.toString())
            }
            tableRowModel.copy(values = updatedValues)
        } else {
            tableRowModel
        }
    }

    return if (hasTotalRow) {
        copy(tableRows = tableRows.dropLast(1)).withTotalsRow(resourceManager)
    } else {
        copy(tableRows = tableRows)
    }
}
