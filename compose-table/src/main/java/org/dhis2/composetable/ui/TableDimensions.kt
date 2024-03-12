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
    val defaultHeaderHeight: Int = 36,
    val defaultLegendCornerSize: Dp = 2.dp,
    val defaultLegendBorderWidth: Dp = 8.dp,
    val defaultHeaderTextSize: TextUnit = 12.sp,
    val defaultRowHeaderTextSize: TextUnit = 12.sp,
    val defaultCellTextSize: TextUnit = 12.sp,
    val totalWidth: Int = 0,
    val cellVerticalPadding: Dp = 4.dp,
    val cellHorizontalPadding: Dp = 4.dp,
    val tableBottomPadding: Dp = 200.dp,
    val extraWidths: Map<String, Int> = emptyMap(),
    val rowHeaderWidths: Map<String, Int> = emptyMap(),
    val columnWidth: Map<String, Map<Int, Int>> = emptyMap(),
    val minRowHeaderWidth: Int = 130,
    val minColumnWidth: Int = 130,
    val maxRowHeaderWidth: Int = Int.MAX_VALUE,
    val maxColumnWidth: Int = Int.MAX_VALUE,
    val tableEndExtraScroll: Dp = 6.dp,
) {

    private var currentExtraSize: MutableMap<String, Int> = mutableMapOf()
    private fun extraWidthInTable(tableId: String): Int = extraWidths[tableId] ?: 0

    fun rowHeaderWidth(tableId: String): Int {
        return (rowHeaderWidths[tableId] ?: defaultRowHeaderWidth) + extraWidthInTable(tableId)
    }

    fun defaultCellWidthWithExtraSize(
        tableId: String,
        totalColumns: Int,
        hasExtra: Boolean = false,
    ): Int = defaultCellWidth +
        extraSize(tableId, totalColumns, hasExtra) +
        extraWidthInTable(tableId)
    fun columnWidthWithTableExtra(tableId: String, column: Int? = null): Int =
        (columnWidth[tableId]?.get(column) ?: defaultCellWidth) + extraWidthInTable(tableId)

    fun headerCellWidth(
        tableId: String,
        column: Int,
        headerRowColumns: Int,
        totalColumns: Int,
        hasTotal: Boolean = false,
    ): Int {
        val rowHeaderRatio = totalColumns / headerRowColumns

        val result = when {
            rowHeaderRatio != 1 -> {
                val maxColumn = rowHeaderRatio * (1 + column) - 1
                val minColumn = rowHeaderRatio * column
                (minColumn..maxColumn).sumOf {
                    columnWidthWithTableExtra(tableId, it) +
                        extraSize(tableId, totalColumns, hasTotal)
                }
            }
            else -> columnWidthWithTableExtra(tableId, column) +
                extraSize(tableId, totalColumns, hasTotal)
        }
        return result
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun headerCellWidth(headerRowColumns: Int, totalColumns: Int): Int {
        val fullWidth = defaultCellWidth * totalColumns
        return fullWidth / headerRowColumns
    }

    fun extraSize(tableId: String, totalColumns: Int, hasTotal: Boolean): Int {
        val screenWidth = totalWidth
        val tableWidth = tableWidth(tableId, totalColumns, hasTotal)

        return if (tableWidth < screenWidth && columnWidth[tableId]?.isEmpty() != false) {
            val totalColumnCount = 1.takeIf { hasTotal } ?: 0
            val columnsCount = totalColumns + totalColumnCount
            ((screenWidth - tableWidth) / columnsCount).also {
                currentExtraSize[tableId] = it
            }
        } else {
            0
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun tableWidth(tableId: String, totalColumns: Int, hasTotal: Boolean): Int {
        val totalCellWidth = defaultCellWidth.takeIf { hasTotal } ?: 0
        return rowHeaderWidth(tableId) + defaultCellWidth * totalColumns + totalCellWidth
    }

    fun updateAllWidthBy(tableId: String, widthOffset: Float): TableDimensions {
        val newWidth = (extraWidths[tableId] ?: 0) + widthOffset - 11
        val newMap = extraWidths.toMutableMap()
        newMap[tableId] = newWidth.toInt()
        return copy(extraWidths = newMap)
    }

    fun updateHeaderWidth(tableId: String, widthOffset: Float): TableDimensions {
        val newWidth = (rowHeaderWidths[tableId] ?: defaultRowHeaderWidth) + widthOffset - 11
        val newMap = rowHeaderWidths.toMutableMap()
        newMap[tableId] = newWidth.toInt()
        return copy(rowHeaderWidths = newMap)
    }

    fun updateColumnWidth(tableId: String, column: Int, widthOffset: Float): TableDimensions {
        val newWidth = (columnWidth[tableId]?.get(column) ?: defaultCellWidth) + widthOffset - 11 +
            (currentExtraSize[tableId] ?: 0)
        val newMap = columnWidth.toMutableMap()
        val tableColumnMap = columnWidth[tableId]?.toMutableMap() ?: mutableMapOf()
        tableColumnMap[column] = newWidth.toInt()
        newMap[tableId] = tableColumnMap
        return this.copy(columnWidth = newMap)
    }

    fun hasOverriddenWidths(tableId: String): Boolean {
        return rowHeaderWidths.containsKey(tableId) ||
            columnWidth.containsKey(tableId) ||
            extraWidths.containsKey(tableId)
    }

    fun resetWidth(tableId: String): TableDimensions {
        val newExtraWidths = extraWidths.toMutableMap()
        val newColumnMap = columnWidth.toMutableMap()
        val newRowHeaderMap = rowHeaderWidths.toMutableMap()
        newExtraWidths.remove(tableId)
        newColumnMap.remove(tableId)
        newRowHeaderMap.remove(tableId)
        return this.copy(
            extraWidths = newExtraWidths,
            rowHeaderWidths = newRowHeaderMap,
            columnWidth = newColumnMap,
        )
    }

    fun canUpdateRowHeaderWidth(tableId: String, widthOffset: Float): Boolean {
        val desiredDimension = updateHeaderWidth(tableId = tableId, widthOffset = widthOffset)
        return desiredDimension.rowHeaderWidth(tableId) in minRowHeaderWidth..maxRowHeaderWidth
    }

    fun canUpdateColumnHeaderWidth(
        tableId: String,
        currentOffsetX: Float,
        columnIndex: Int,
        totalColumns: Int,
        hasTotal: Boolean,
    ): Boolean {
        val desiredDimension = updateColumnWidth(
            tableId = tableId,
            widthOffset = currentOffsetX,
            column = columnIndex,
        )
        return desiredDimension.columnWidthWithTableExtra(
            tableId,
            columnIndex,
        ) + extraSize(tableId, totalColumns, hasTotal) in minColumnWidth..maxColumnWidth
    }

    fun canUpdateAllWidths(tableId: String, widthOffset: Float): Boolean {
        val desiredDimension = updateAllWidthBy(tableId = tableId, widthOffset = widthOffset)
        return desiredDimension.rowHeaderWidth(tableId) in minRowHeaderWidth..maxRowHeaderWidth &&
            desiredDimension.columnWidthWithTableExtra(tableId) in minColumnWidth..maxColumnWidth &&
            desiredDimension.columnWidth[tableId]?.all { (column, _) ->
                desiredDimension.columnWidthWithTableExtra(
                    tableId,
                    column,
                ) in minColumnWidth..maxColumnWidth
            } ?: true
    }

    fun getRowHeaderWidth(tableId: String): Int {
        return rowHeaderWidths[tableId] ?: defaultRowHeaderWidth
    }

    fun getColumnWidth(tableId: String, column: Int): Int {
        return columnWidth[tableId]?.get(column) ?: defaultCellWidth
    }

    fun getExtraWidths(tableId: String): Int {
        return extraWidths[tableId] ?: 0
    }
}

val LocalTableDimensions = compositionLocalOf { TableDimensions() }
