package org.dhis2.composetable.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.dhis2.composetable.model.TableCornerUiState
import org.dhis2.composetable.ui.modifiers.cornerBackground

@Composable
fun TableCorner(
    modifier: Modifier = Modifier,
    tableCornerUiState: TableCornerUiState,
    tableId: String,
    onClick: () -> Unit
) {
    val isSelected = LocalTableSelection.current is TableSelection.AllCellSelection
    Box(
        modifier = modifier
            .cornerBackground(
                isSelected = isSelected,
                selectedColor = LocalTableColors.current.primaryLight,
                defaultColor = LocalTableColors.current.tableBackground
            )
            .width(
                with(LocalDensity.current) {
                    TableTheme.dimensions
                        .rowHeaderWidth(tableId)
                        .toDp()
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.CenterEnd
    ) {
        Divider(
            modifier
                .fillMaxHeight()
                .width(1.dp),
            color = TableTheme.colors.primary
        )
        if (isSelected) {
            VerticalResizingRule(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .zIndex(1f),
                checkMaxMinCondition = { dimensions, currentOffsetX ->
                    dimensions.canUpdateAllWidths(
                        tableId = tableId,
                        widthOffset = currentOffsetX
                    )
                },
                onHeaderResize = { newValue ->
                    tableCornerUiState.onTableResize(newValue)
                },
                onResizing = tableCornerUiState.onResizing
            )
        }
    }
}
