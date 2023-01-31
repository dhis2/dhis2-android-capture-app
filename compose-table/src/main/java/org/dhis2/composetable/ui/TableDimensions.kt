package org.dhis2.composetable.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class TableDimensions(
    val tableHorizontalPadding: Dp = 16.dp,
    val tableVerticalPadding: Dp = 16.dp,
    val defaultCellWidth: Int = 160,
    val defaultCellHeight: Dp = 36.dp,
    val defaultRowHeaderWidth: Int = 275,
    val defaultHeaderHeight: Int = 83,
    val defaultLegendCornerSize: Dp = 2.dp,
    val defaultLegendBorderWidth: Dp = 8.dp,
    val defaultHeaderTextSize: TextUnit = 12.sp,
    val defaultRowHeaderTextSize: TextUnit = 12.sp,
    val defaultCellTextSize: TextUnit = 12.sp,
    val totalWidth: Int = 0,
    val cellVerticalPadding: Dp = 4.dp,
    val cellHorizontalPadding: Dp = 4.dp,
    val tableBottomPadding: Dp = 200.dp,
    val columnWidth: Map<String, Map<Int, Int>> = emptyMap()
) {
    fun defaultCellWidthWithExtraSize(
        tableId: String,
        column: Int? = null,
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Int {
        return columnWidth[tableId]?.get(column) ?: defaultCellWidth.withExtraSize(
            totalColumns,
            hasExtra
        )
    }

    fun headerCellWidth(
        tableId: String,
        column: Int,
        headerRowColumns: Int,
        totalColumns: Int,
        hasTotal: Boolean = false
    ): Int {
        val fullWidth = defaultCellWidth * totalColumns
        val rowHeaderRatio = totalColumns / headerRowColumns

        return when {
            rowHeaderRatio != 1 -> {
                val maxColumn = rowHeaderRatio * (1 + column) - 1
                val minColumn = rowHeaderRatio * column
                val resizedColumns = columnWidth[tableId]?.filter { (columnIndex, width) ->
                    columnIndex in minColumn..maxColumn
                } ?: emptyMap()
                val totalResizedWidth = resizedColumns.values.sum()
                val remainingWidth = (rowHeaderRatio - resizedColumns.size) * defaultCellWidth
                totalResizedWidth + remainingWidth
                    .withExtraSize(totalColumns, hasTotal, 1)
            }
            else ->
                columnWidth[tableId]?.get(column) ?: (fullWidth / headerRowColumns)
                    .withExtraSize(totalColumns, hasTotal, rowHeaderRatio)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun headerCellWidth(
        headerRowColumns: Int,
        totalColumns: Int
    ): Int {
        val fullWidth = defaultCellWidth * totalColumns
        return fullWidth / headerRowColumns
    }

    private fun Int.withExtraSize(
        totalColumns: Int,
        hasExtra: Boolean = false,
        extraWidthRatio: Int = 1
    ): Int {
        return this + extraWidth(totalColumns, hasExtra) * extraWidthRatio
    }

    private fun extraWidth(
        totalColumns: Int,
        hasTotal: Boolean
    ): Int {
        val screenWidth = totalWidth
        val tableWidth = tableWidth(totalColumns, hasTotal)

        return if (tableWidth < screenWidth) {
            val totalColumnCount = 1.takeIf { hasTotal } ?: 0
            val columnsCount = totalColumns + totalColumnCount
            (screenWidth - tableWidth) / columnsCount
        } else {
            0
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun tableWidth(
        totalColumns: Int,
        hasTotal: Boolean
    ): Int {
        val totalCellWidth = defaultCellWidth.takeIf { hasTotal } ?: 0

        return defaultRowHeaderWidth + defaultCellWidth * totalColumns + totalCellWidth
    }

    fun updateHeaderWidth(widthOffset: Float): TableDimensions {
        val newWidth = defaultRowHeaderWidth + widthOffset - 11
        return copy(defaultRowHeaderWidth = newWidth.toInt())
    }

    fun updateColumnWidth(
        tableId: String,
        column: Int,
        widthOffset: Float
    ): TableDimensions {
        val newWidth = (columnWidth[tableId]?.get(column) ?: defaultCellWidth) + widthOffset - 11
        val newMap = columnWidth.toMutableMap()
        val tableColumnMap = columnWidth[tableId]?.toMutableMap() ?: mutableMapOf()
        tableColumnMap[column] = newWidth.toInt()
        newMap[tableId] = tableColumnMap
        return this.copy(columnWidth = newMap)
    }
}

fun TableDimensions.withRowHeaderWidth(defaultRowHeaderWidth: Int?): TableDimensions {
    return defaultRowHeaderWidth?.let { this.copy(defaultRowHeaderWidth = defaultRowHeaderWidth) }
        ?: this
}

fun TableDimensions.withColumnWidth(columnWidth: Map<String, Map<Int, Int>>?): TableDimensions {
    return columnWidth?.let { this.copy(columnWidth = columnWidth) } ?: this
}

val LocalTableDimensions = compositionLocalOf { TableDimensions() }
