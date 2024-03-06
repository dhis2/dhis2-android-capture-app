package org.dhis2.composetable.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import org.dhis2.composetable.model.ItemHeaderUiState
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.ui.semantics.ROW_TEST_TAG

@Composable
fun TableItemRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    rowModel: TableRowModel,
    rowHeaderCellStyle: @Composable
    (rowHeaderIndex: Int?) -> CellStyle,
    onRowHeaderClick: (rowHeaderIndex: Int?) -> Unit,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit,
    onHeaderResize: (Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit
) {
    Column(
        Modifier
            .testTag("$ROW_TEST_TAG${rowModel.rowHeader.row}")
            .width(IntrinsicSize.Min)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .zIndex(1f)
            ) {
                ItemHeader(
                    ItemHeaderUiState(
                        tableId = tableModel.id ?: "",
                        rowHeader = rowModel.rowHeader,
                        cellStyle = rowHeaderCellStyle(rowModel.rowHeader.row),
                        width = with(LocalDensity.current) {
                            TableTheme.dimensions.rowHeaderWidth(tableModel.id ?: "").toDp()
                        },
                        maxLines = rowModel.maxLines,
                        onCellSelected = onRowHeaderClick,
                        onDecorationClick = onDecorationClick,
                        onHeaderResize = onHeaderResize,
                        onResizing = onResizing
                    )
                )
            }
            ItemValues(
                tableId = tableModel.id ?: "",
                horizontalScrollState = horizontalScrollState,
                cellValues = rowModel.values,
                overridenValues = tableModel.overwrittenValues,
                maxLines = rowModel.maxLines,
                headerExtraSize = TableTheme.dimensions.extraSize(
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                ),
                options = rowModel.dropDownOptions ?: emptyList()
            )
        }
        if (!rowModel.isLastRow) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = TableTheme.dimensions.tableEndExtraScroll)
            )
        }
    }
}
