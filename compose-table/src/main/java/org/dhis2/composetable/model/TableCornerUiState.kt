package org.dhis2.composetable.model

data class TableCornerUiState(
    val isSelected: Boolean = false,
    val onTableResize: (Float) -> Unit,
    val onResizing: (ResizingCell?) -> Unit
)
