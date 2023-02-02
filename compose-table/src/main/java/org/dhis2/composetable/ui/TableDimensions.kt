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
    val tableBottomPadding: Dp = 200.dp
) {

    fun defaultCellWidthWithExtraSize(
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Int {
        return defaultCellWidth.withExtraSize(totalColumns, hasExtra)
    }

    fun headerCellWidth(
        headerRowColumns: Int,
        totalColumns: Int,
        hasTotal: Boolean = false
    ): Int {
        val fullWidth = defaultCellWidth * totalColumns
        val rowHeaderRatio = totalColumns / headerRowColumns
        return (fullWidth / headerRowColumns)
            .withExtraSize(totalColumns, hasTotal, rowHeaderRatio)
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
}

val LocalTableDimensions = compositionLocalOf { TableDimensions() }
