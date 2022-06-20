package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import java.util.SortedMap
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

class TableDataToTableModelMapper {
    fun map(tableData: TableData): TableModel {
        val tableHeader = TableHeader(
            rows = tableData.columnHeaders()?.map { catOptions ->
                TableHeaderRow(
                    cells = catOptions.distinctBy { it.uid() }
                        .filter { it.uid() != null && it.uid().isNotEmpty() }
                        .map { categoryOption ->
                            TableHeaderCell(categoryOption.displayName()!!)
                        }
                )
            } ?: emptyList(),
            hasTotals = tableData.showRowTotals
        )

        val tableRows = tableData.rows()?.mapIndexed { index, dataElement ->
            TableRowModel(
                rowHeader = RowHeader(dataElement.displayName()!!),
                values = tableData.fieldViewModels[index].mapIndexed { columnIndex, field ->
                    columnIndex to TableCell(
                        tableData.cells[index][columnIndex],
                        field.editable(),
                        field.mandatory(),
                        field.error()
                    )
                }.toMap()
            )
        } ?: emptyList()

        return TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }

    fun map(tableData: SortedMap<String?, String>): TableModel {
        val tableHeader = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableHeaderCell("Value")
                    )
                )
            )
        )

        val tableRows = tableData.map { (indicatorName, indicatorValue) ->
            TableRowModel(
                rowHeader = RowHeader(indicatorName!!),
                values = mapOf(Pair(0, TableCell(indicatorValue)))
            )
        }

        return TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }
}
