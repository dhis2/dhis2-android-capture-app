package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import java.util.SortedMap

class TableDataToTableModelMapper(val resources: ResourceManager) {
    fun map(tableData: TableData): TableModel {
        val tableHeader = TableHeader(
            rows = tableData.columnHeaders()?.map { catOptions ->
                TableHeaderRow(
                    cells = catOptions.distinctBy { it.uid() }
                        .filter { it.uid() != null && it.uid().isNotEmpty() }
                        .map { categoryOption ->
                            TableCell(value = categoryOption.displayName()!!)
                        }
                )
            } ?: emptyList(),
            hasTotals = tableData.showRowTotals
        )

        val tableRows = tableData.rows()?.mapIndexed { rowIndex, dataElement ->
            TableRowModel(
                rowHeader = RowHeader(
                    dataElement.displayName()!!,
                    rowIndex,
                    tableData.hasDataElementDecoration && dataElement.displayDescription() != null
                ),
                values = tableData.fieldViewModels[rowIndex].mapIndexed { columnIndex, field ->
                    columnIndex to TableCell(
                        id = field.uid(),
                        row = rowIndex,
                        column = columnIndex,
                        value = mapFieldValueToUser(field, dataElement),
                        editable = field.editable(),
                        mandatory = field.mandatory(),
                        error = field.error()
                    )
                }.toMap()
            )
        } ?: emptyList()

        return TableModel(
            id = tableData.catCombo()?.uid(),
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }

    private fun mapFieldValueToUser(field: FieldViewModel, dataElement: DataElement): String? {
        return when (dataElement.valueType()) {
            ValueType.BOOLEAN,
            ValueType.TRUE_ONLY -> {
                if (!field.value().isNullOrEmpty()) {
                    if (field.value().toBoolean()) {
                        resources.getString(R.string.yes)
                    } else {
                        resources.getString(R.string.no)
                    }
                } else {
                    field.value()
                }
            }
            else -> field.value()
        }
    }

    fun map(tableData: SortedMap<String?, String>): TableModel {
        val tableHeader = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableCell(value = "Value")
                    )
                )
            )
        )

        val tableRows = tableData.map { (indicatorName, indicatorValue) ->
            TableRowModel(
                rowHeader = RowHeader(indicatorName!!),
                values = mapOf(Pair(0, TableCell(value = indicatorValue)))
            )
        }

        return TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }
}
