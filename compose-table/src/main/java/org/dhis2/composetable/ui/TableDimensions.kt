package org.dhis2.composetable.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class TableDimensions(
    val tableHorizontalPadding: Dp = 16.dp,
    val tableVerticalPadding: Dp = 16.dp,
    val defaultCellWidth: Dp = 58.dp,
    val defaultCellHeight: Dp = 36.dp,
    val defaultRowHeaderWidth: Dp = 100.dp,
    val defaultHeaderHeight: Dp = 30.dp,
    val defaultLegendCornerSize: Dp = 2.dp,
    val defaultLegendBorderWidth: Dp = 8.dp,
    val defaultHeaderTextSize: TextUnit = 12.sp,
    val defaultRowHeaderTextSize: TextUnit = 12.sp,
    val defaultCellTextSize: TextUnit = 12.sp,
    val totalWidth: Dp = 0.dp
) {

    fun defaultCellWidthWithExtraSize(
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Dp {
        return defaultCellWidth.withExtraSize(totalColumns, hasExtra)
    }

    fun defaultRowHeaderCellWidthWithExtraSize(
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Dp {
        return defaultRowHeaderWidth
    }

    fun headerCellWidth(
        headerRowColumns: Int,
        totalColumns: Int,
        hasTotal: Boolean = false
    ): Dp {
        val fullWidth = defaultCellWidth * totalColumns
        val rowHeaderRatio = totalColumns / headerRowColumns
        return (fullWidth / headerRowColumns)
            .withExtraSize(totalColumns, hasTotal, rowHeaderRatio)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun headerCellWidth(
        headerRowColumns: Int,
        totalColumns: Int
    ): Dp {
        val fullWidth = defaultCellWidth * totalColumns
        return fullWidth / headerRowColumns
    }

    private fun Dp.withExtraSize(
        totalColumns: Int,
        hasExtra: Boolean = false,
        extraWidthRatio: Int = 1
    ): Dp {
        return this + extraWidth(totalColumns, hasExtra) * extraWidthRatio
    }

    private fun extraWidth(
        totalColumns: Int,
        hasTotal: Boolean
    ): Dp {
        val screenWidth = totalWidth
        val tableWidth = tableWidth(totalColumns, hasTotal)

        return if (tableWidth < screenWidth) {
            val totalColumnCount = 1.takeIf { hasTotal } ?: 0
            val columnsCount = totalColumns + totalColumnCount
            (screenWidth - tableWidth) / columnsCount
        } else {
            0.dp
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun tableWidth(
        totalColumns: Int,
        hasTotal: Boolean
    ): Dp {
        val totalCellWidth = defaultCellWidth.takeIf { hasTotal } ?: 0.dp

        return defaultRowHeaderWidth + defaultCellWidth * totalColumns + totalCellWidth
    }
}

val LocalTableDimensions = staticCompositionLocalOf { TableDimensions() }
