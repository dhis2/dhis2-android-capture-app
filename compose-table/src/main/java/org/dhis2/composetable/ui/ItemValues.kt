package org.dhis2.composetable.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import org.dhis2.composetable.model.DropdownOption
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG

@Composable
fun ItemValues(
    tableId: String,
    horizontalScrollState: ScrollState,
    maxLines: Int,
    cellValues: Map<Int, TableCell>,
    overridenValues: Map<Int, TableCell>,
    tableHeaderModel: TableHeader,
    options: List<DropdownOption>,
    headerLabel: String,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState),
    ) {
        repeat(
            times = cellValues.size,
            action = { columnIndex ->
                val cellValue =
                    if (overridenValues[columnIndex]?.id == cellValues[columnIndex]?.id) {
                        overridenValues[columnIndex]
                    } else {
                        cellValues[columnIndex]
                    } ?: TableCell(value = "", column = columnIndex)

                key("$tableId$CELL_TEST_TAG${cellValue.row}${cellValue.column}") {
                    TableCell(
                        tableId = tableId,
                        cell = cellValue,
                        maxLines = maxLines,
                        headerExtraSize = TableTheme.dimensions.extraSize(
                            tableId,
                            tableHeaderModel.tableMaxColumns(),
                            tableHeaderModel.hasTotals,
                            columnIndex,
                        ),
                        options = options,
                        headerLabel = headerLabel,
                    )
                }
            },
        )
        Spacer(Modifier.size(TableTheme.dimensions.tableEndExtraScroll))
    }
}
