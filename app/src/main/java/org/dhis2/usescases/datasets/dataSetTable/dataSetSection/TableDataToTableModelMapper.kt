package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import java.util.SortedMap
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement

class TableDataToTableModelMapper(
    val resources: ResourceManager,
    val repository: DataValueRepository
) {
    fun map(tableData: TableData): TableModel {
        val tableHeader = TableHeader(
            rows = tableData.columnHeaders()?.map { catOptions ->
                TableHeaderRow(
                    cells = catOptions.distinctBy { it.uid() }
                        .filter { it.uid() != null && it.uid().isNotEmpty() }
                        .map { categoryOption ->
                            TableHeaderCell(value = categoryOption.displayName()!!)
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
                        error = field.error(),
                        isReadOnly = isReadyOnly(dataElement)
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

    private fun isReadyOnly(dataElement: DataElement): Boolean {
        return if (dataElement.optionSetUid() != null) {
            true
        } else when (dataElement.valueType()) {
            ValueType.BOOLEAN,
            ValueType.TRUE_ONLY,
            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME,
            ValueType.COORDINATE,
            ValueType.ORGANISATION_UNIT,
            ValueType.AGE,
            ValueType.IMAGE,
            ValueType.FILE_RESOURCE,
            ValueType.TRACKER_ASSOCIATE,
            ValueType.REFERENCE,
            ValueType.GEOJSON -> true
            else -> false
        }
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
            ValueType.AGE -> {
                if (!field.value().isNullOrEmpty()) {
                    DateUtils.uiDateFormat().format(field.value()!!.toDate())
                } else {
                    field.value()
                }
            }
            ValueType.IMAGE,
            ValueType.FILE_RESOURCE,
            ValueType.TRACKER_ASSOCIATE,
            ValueType.REFERENCE,
            ValueType.GEOJSON -> resources.getString(R.string.unsupported_value_type)
            ValueType.ORGANISATION_UNIT -> {
                if (!field.value().isNullOrEmpty()) {
                    repository.getOrgUnitById(field.value()!!)
                } else {
                    field.value()
                }
            }
            ValueType.IMAGE,
            ValueType.FILE_RESOURCE,
            ValueType.TRACKER_ASSOCIATE,
            ValueType.REFERENCE,
            ValueType.GEOJSON-> resources.getString(R.string.unsupported_value_type)
            else -> field.value()
        }
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
