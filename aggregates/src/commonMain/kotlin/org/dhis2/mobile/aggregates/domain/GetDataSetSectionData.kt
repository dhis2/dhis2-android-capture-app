package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.ValueParser
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

internal class GetDataSetSectionData(
    private val datasetUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
    private val valueParser: ValueParser,
) {
    suspend operator fun invoke(sectionUid: String): List<TableModel> {
        var absoluteRowIndex = 0
        val dataSetInstanceConfiguration =
            dataSetInstanceRepository.dataSetInstanceConfiguration(
                datasetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
                sectionUid,
            )

        val dataSetInstanceSectionConfiguration =
            dataSetInstanceRepository.dataSetInstanceSectionConfiguration(
                sectionUid,
            )

        return dataSetInstanceRepository.getDataSetInstanceSectionCells(
            dataSetInstanceConfiguration.allDataSetElements,
            datasetUid,
            sectionUid,
        ).map { tableGroup ->

            val headerRows = dataSetInstanceRepository.getTableGroupHeaders(
                tableGroup.subgroups,
            ).map { headerColumn ->
                TableHeaderRow(
                    cells = headerColumn.map { label ->
                        TableHeaderCell(
                            value = label,
                        )
                    },
                )
            }

            val tableHeader = TableHeader(
                rows = headerRows,
                hasTotals = dataSetInstanceSectionConfiguration?.showRowTotals == true,
            )

            val headerCombinations = dataSetInstanceRepository.categoryOptionCombinations(
                categoryUids = tableGroup.subgroups,
            )

            val tableRows = tableGroup.cellElements
                .mapIndexed { rowIndex, cellElement ->
                    TableRowModel(
                        rowHeader = RowHeader(
                            id = cellElement.uid,
                            title = cellElement.label,
                            row = absoluteRowIndex,
                            showDecoration = dataSetInstanceConfiguration.hasDataElementDecoration &&
                                cellElement.description != null,
                            description = cellElement.description,
                        ),
                        values = buildMap {
                            repeat(tableHeader.tableMaxColumns()) { columnIndex ->
                                val errorsAndWarnings = dataSetInstanceRepository.conflicts(
                                    dataSetUid = datasetUid,
                                    periodId = periodId,
                                    orgUnitUid = orgUnitUid,
                                    attrOptionComboUid = attrOptionComboUid,
                                    dataElementUid = cellElement.uid,
                                    categoryOptionComboUid = headerCombinations[columnIndex],
                                )

                                put(
                                    key = columnIndex,
                                    value = TableCell(
                                        id = cellElement.uid,
                                        row = rowIndex,
                                        column = columnIndex,
                                        value = dataSetInstanceRepository.cellValue(
                                            periodId,
                                            orgUnitUid,
                                            cellElement.uid,
                                            headerCombinations[columnIndex],
                                            attrOptionComboUid,
                                        )?.let { value ->
                                            valueParser.parseValue(cellElement.uid, value)
                                        },
                                        editable = dataSetInstanceConfiguration.isCellEditable(
                                            cellElement.uid,
                                        ),
                                        mandatory = dataSetInstanceConfiguration.isMandatory(
                                            rowId = cellElement.uid,
                                            columnId = headerCombinations[columnIndex],
                                        ),
                                        error = errorsAndWarnings.first.joinToString(
                                            separator = ".\n",
                                        ).takeIf { it.isNotEmpty() },
                                        warning = errorsAndWarnings.second.joinToString(
                                            separator = ".\n",
                                        ).takeIf { it.isNotEmpty() },
                                        legendColor = null,
                                        isMultiText = cellElement.isMultiText,
                                    ),
                                )
                            }
                        },
                        isLastRow = false, // TODO: This should not be needed
                        maxLines = 3,
                        dropDownOptions = null, /*TODO: This has to be requested on demand*/
                    ).also {
                        absoluteRowIndex += 1
                    }
                }

            TableModel(
                id = tableGroup.uid,
                title = tableGroup.label,
                tableHeaderModel = tableHeader,
                tableRows = tableRows,
                overwrittenValues = emptyMap(), /*TODO: This seems to not be used at all*/
            )
        }
    }
}
