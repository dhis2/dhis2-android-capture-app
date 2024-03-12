package org.dhis2.composetable.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.dhis2.composetable.R
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.TableCornerUiState
import org.dhis2.composetable.model.TableModel

@Composable
fun TableHeaderRow(
    modifier: Modifier = Modifier,
    cornerUiState: TableCornerUiState,
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    cellStyle: @Composable
    (headerColumnIndex: Int, headerRowIndex: Int) -> CellStyle,
    onTableCornerClick: () -> Unit = {},
    onHeaderCellClick: (headerColumnIndex: Int, headerRowIndex: Int) -> Unit = { _, _ -> },
    onHeaderResize: (Int, Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit,
    onResetResize: () -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize(),
    ) {
        val isHeaderActionEnabled = TableTheme.configuration.headerActionsEnabled
        val (tableActions, tableCorner, header) = createRefs()

        if (isHeaderActionEnabled) {
            TableActions(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .constrainAs(tableActions) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                title = tableModel.title,
                actionIcons = {
                    if (TableTheme.dimensions.hasOverriddenWidths(tableModel.id ?: "")) {
                        IconButton(onClick = onResetResize) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = R.drawable.ic_restart_alt,
                                ),
                                contentDescription = "",
                                tint = Color.Black.copy(alpha = 0.87f),
                            )
                        }
                    }
                },
            )
        }

        TableCorner(
            modifier = Modifier
                .constrainAs(tableCorner) {
                    if (isHeaderActionEnabled) {
                        top.linkTo(tableActions.bottom)
                    } else {
                        top.linkTo(parent.top)
                    }
                    start.linkTo(parent.start)
                    end.linkTo(header.start)
                    bottom.linkTo(header.bottom)
                    height = Dimension.fillToConstraints
                }
                .zIndex(1f),
            tableCornerUiState = cornerUiState,
            tableId = tableModel.id ?: "",
            onClick = onTableCornerClick,
        )

        TableHeader(
            tableId = tableModel.id,
            modifier = Modifier
                .constrainAs(header) {
                    if (isHeaderActionEnabled) {
                        top.linkTo(tableActions.bottom)
                    } else {
                        top.linkTo(parent.top)
                    }
                    start.linkTo(tableCorner.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState,
            cellStyle = cellStyle,
            onHeaderCellSelected = onHeaderCellClick,
            onHeaderResize = onHeaderResize,
            onResizing = onResizing,
        )
    }
}
