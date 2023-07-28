package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import java.util.SortedMap
import org.dhis2.R
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

class TableDataToTableModelMapper(val mapFieldValueToUser: MapFieldValueToUser) {
    operator fun invoke(tableData: TableData): TableModel {
        val tableHeader = TableHeader(
            rows = tableData.columnHeaders()?.map { catOptions ->
                TableHeaderRow(
                    cells = catOptions.distinctBy { it.uid() }
                        .filter { it.uid() != null && it.uid().isNotEmpty() }
                        .map { categoryOption ->
                            val headerLabel = if (tableData.catCombo()?.isDefault == true) {
                                mapFieldValueToUser.getDefaultHeaderLabel()
                            } else {
                                categoryOption.displayName()!!
                            }
                            TableHeaderCell(value = headerLabel)
                        }
                )
            } ?: emptyList(),
            hasTotals = tableData.showRowTotals
        )

        val tableRows = tableData.rows()?.mapIndexed { rowIndex, dataElement ->
            TableRowModel(
                rowHeader = RowHeader(
                    dataElement.uid(),
                    dataElement.displayFormName() ?: dataElement.uid(),
                    rowIndex,
                    tableData.hasDataElementDecoration && dataElement.displayDescription() != null,
                    dataElement.displayDescription()
                        ?: mapFieldValueToUser.resources.getString(R.string.empty_description)
                ),
                values = tableData.fieldViewModels[rowIndex].mapIndexed { columnIndex, field ->
                    columnIndex to TableCell(
                        id = field.uid(),
                        row = rowIndex,
                        column = columnIndex,
                        value = mapFieldValueToUser.map(field, dataElement),
                        editable = tableData.accessDataWrite && field.editable()!!,
                        mandatory = field.mandatory(),
                        error = field.error(),
                        warning = field.warning()
                    )
                }.toMap(),
                isLastRow = rowIndex == (tableData.rows()!!.size - 1),
                maxLines = 3,
                dropDownOptions = tableData.fieldViewModels[rowIndex][0].options()
            )
        } ?: emptyList()

        return TableModel(
            id = tableData.catCombo()?.uid(),
            title = tableData.catCombo()?.displayName() ?: "",
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }

    fun map(tableData: SortedMap<String?, String>): TableModel {
        val tableHeader = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableHeaderCell(value = "Value")
                    )
                )
            )
        )
        val tableRows = tableData.map { (indicatorName, indicatorValue) ->
            TableRowModel(
                rowHeader = RowHeader(id = indicatorName, title = indicatorName!!),
                values = mapOf(
                    0 to TableCell(id = indicatorName, value = indicatorValue, editable = false)
                )
            )
        }

        return TableModel(
            title = "Indicators",
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }
}
