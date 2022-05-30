package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import java.util.SortedMap
import org.dhis2.compose_table.model.TableHeader
import org.dhis2.compose_table.model.TableHeaderCell
import org.dhis2.compose_table.model.TableHeaderRow
import org.dhis2.compose_table.model.TableModel
import org.dhis2.compose_table.model.TableRowModel

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
                rowHeader = dataElement.displayName()!!,
                values = tableData.cells[index].mapIndexed { columnIndex, s ->
                    columnIndex to TableHeaderCell(s)
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
                rowHeader = indicatorName!!,
                values = mapOf(Pair(0, TableHeaderCell(indicatorValue)))
            )
        }

        return TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }
}
