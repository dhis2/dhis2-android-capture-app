package org.dhis2.mobile.aggregates.model

sealed class ResizeSaveDimension {
    data class Table(
        val saveTableWidth: Map<String, Float>?,
    ) : ResizeSaveDimension()

    data class RowHeader(
        val widthForSection: Map<String, Float>?,
    ) : ResizeSaveDimension()

    data class ColumnHeader(
        val columnWidthForSection: Map<String, Map<Int, Float>>?,
    ) : ResizeSaveDimension()

    fun tableWidth() =
        when (this) {
            is ColumnHeader -> emptyMap()
            is RowHeader -> emptyMap()
            is Table -> saveTableWidth ?: emptyMap()
        }

    fun rowHeaderWidths() =
        when (this) {
            is ColumnHeader -> emptyMap()
            is RowHeader -> widthForSection ?: emptyMap()
            is Table -> emptyMap()
        }

    fun columnWidths() =
        when (this) {
            is ColumnHeader -> columnWidthForSection ?: emptyMap()
            is RowHeader -> emptyMap()
            is Table -> emptyMap()
        }
}
