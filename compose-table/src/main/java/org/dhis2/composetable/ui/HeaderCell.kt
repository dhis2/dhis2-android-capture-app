package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.dhis2.composetable.model.ItemColumnHeaderUiState
import org.dhis2.composetable.ui.semantics.columnBackground
import org.dhis2.composetable.ui.semantics.columnIndexHeader
import org.dhis2.composetable.ui.semantics.rowIndexHeader
import org.dhis2.composetable.ui.semantics.tableIdColumnHeader

@Composable
fun HeaderCell(itemHeaderUiState: ItemColumnHeaderUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(with(LocalDensity.current) { itemHeaderUiState.headerMeasures.width.toDp() })
            .height(with(LocalDensity.current) { itemHeaderUiState.headerMeasures.height.toDp() })
            .background(itemHeaderUiState.cellStyle.backgroundColor())
            .testTag(itemHeaderUiState.testTag)
            .semantics {
                itemHeaderUiState.tableId?.let { tableIdColumnHeader = it }
                columnIndexHeader = itemHeaderUiState.columnIndex
                rowIndexHeader = itemHeaderUiState.rowIndex
                columnBackground = itemHeaderUiState.cellStyle.backgroundColor()
            }
            .clickable {
                itemHeaderUiState.onCellSelected(itemHeaderUiState.columnIndex)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .align(Alignment.Center),
            color = itemHeaderUiState.cellStyle.mainColor(),
            text = itemHeaderUiState.headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = TableTheme.dimensions.defaultHeaderTextSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            softWrap = true,
        )
        Divider(
            color = TableTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
        val isSelected = when (LocalTableSelection.current) {
            is TableSelection.AllCellSelection -> false
            else -> LocalTableSelection.current.isHeaderSelected(
                selectedTableId = itemHeaderUiState.tableId ?: "",
                columnIndex = itemHeaderUiState.columnIndex,
                columnHeaderRowIndex = itemHeaderUiState.rowIndex,
            )
        }
        if (isSelected && itemHeaderUiState.isLastRow) {
            VerticalResizingRule(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .zIndex(2f),
                checkMaxMinCondition = itemHeaderUiState.checkMaxCondition,
                onHeaderResize = { newValue ->
                    itemHeaderUiState.onHeaderResize(
                        itemHeaderUiState.columnIndex,
                        newValue,
                    )
                },
                onResizing = itemHeaderUiState.onResizing,
            )
        }
    }
}
