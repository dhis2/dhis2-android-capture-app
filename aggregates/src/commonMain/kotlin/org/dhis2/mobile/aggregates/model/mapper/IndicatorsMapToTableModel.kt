package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.domain.IndicatorMap
import org.dhis2.mobile.aggregates.ui.constants.INDICATOR_TABLE_UID
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCellContent
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

internal suspend fun IndicatorMap.toTableModel(
    resourceManager: ResourceManager,
    absoluteRowIndex: Int,
) = TableModel(
    id = INDICATOR_TABLE_UID,
    title = resourceManager.indicatorsLabel(),
    tableHeaderModel =
        TableHeader(
            rows =
                listOf(
                    TableHeaderRow(
                        cells = listOf(TableHeaderCell(resourceManager.defaultHeaderLabel())),
                    ),
                ),
            extraColumns = emptyList(),
        ),
    tableRows =
        entries.mapIndexed { index, (key, value) ->
            val rowIndex = index + absoluteRowIndex
            TableRowModel(
                rowHeaders =
                    listOf(
                        RowHeader(
                            id = key,
                            title = key,
                            row = rowIndex,
                            column = 0,
                        ),
                    ),
                values =
                    mapOf(
                        0 to
                            TableCell(
                                id = key,
                                row = rowIndex,
                                column = 0,
                                content = TableCellContent.Text(value),
                                editable = false,
                                mandatory = false,
                                legendColor = null,
                            ),
                    ),
            )
        },
)
