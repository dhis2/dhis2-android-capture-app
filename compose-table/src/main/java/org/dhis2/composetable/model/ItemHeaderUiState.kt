package org.dhis2.composetable.model

import androidx.compose.ui.unit.Dp
import org.dhis2.composetable.ui.CellStyle

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
