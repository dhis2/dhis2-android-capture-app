package org.dhis2.composetable.model

import org.dhis2.composetable.ui.CellStyle
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.semantics.HEADER_CELL

data class ItemColumnHeaderUiState(
    val tableId: String?,
    val rowIndex: Int,
    val columnIndex: Int,
    val headerCell: TableHeaderCell,
    val headerMeasures: HeaderMeasures,
    val cellStyle: CellStyle,
    val onCellSelected: (Int) -> Unit,
    val onHeaderResize: (Int, Float) -> Unit,
    val onResizing: (ResizingCell?) -> Unit,
    val isLastRow: Boolean,
    val checkMaxCondition: (TableDimensions, Float) -> Boolean,
) {
    val testTag = "$HEADER_CELL$tableId$rowIndex$columnIndex"
}
