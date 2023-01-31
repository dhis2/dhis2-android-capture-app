package org.dhis2.composetable.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.dhis2.composetable.ui.CellStyle
import org.dhis2.composetable.ui.HEADER_CELL

data class ItemHeaderUiState(
    val tableId: String,
    val rowHeader: RowHeader,
    val cellStyle: CellStyle,
    val width: Dp,
    val maxLines: Int,
    val onCellSelected: (Int?) -> Unit,
    val onDecorationClick: (dialogModel: TableDialogModel) -> Unit,
    val onHeaderResize: (Float) -> Unit,
    val onResizing: (ResizingCell?) -> Unit
)

data class ItemColumnHeaderUiState(
    val tableId: String?,
    val rowIndex: Int,
    val columnIndex: Int,
    val headerCell: TableHeaderCell,
    val headerMeasures: HeaderMeasures,
    val cellStyle: CellStyle,
    val onCellSelected: (Int) -> Unit,
    val onHeaderResize: (Int, Float) -> Unit,
    val onResizing: (ResizingCell?) -> Unit
) {
    val testTag = "$HEADER_CELL$tableId$rowIndex$columnIndex"
    fun isSelected(selectedColor: Color) = cellStyle.backgroundColor() == selectedColor
}

data class TableCornerUiState(
    val isSelected: Boolean = false,
    val onTableResize: (Float) -> Unit,
    val onResizing: (ResizingCell?) -> Unit
)
