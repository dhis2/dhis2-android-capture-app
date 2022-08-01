package org.dhis2.composetable.ui

import android.content.res.Configuration
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
    val defaultCellWidth: Dp = 52.dp,
    val defaultCellHeight: Dp = 36.dp,
    val defaultRowHeaderWidth: Dp = 60.dp,
    val defaultHeaderHeight: Dp = 24.dp,
    val defaultLegendCornerSize: Dp = 2.dp,
    val defaultLegendBorderWidth: Dp = 4.dp,
    val defaultHeaderTextSize: TextUnit = 12.sp,
    val defaultRowHeaderTextSize: TextUnit = 12.sp,
    val defaultCellTextSize: TextUnit = 12.sp
) {

    fun defaultCellWidthWithExtraSize(
        configuration: Configuration,
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Dp {
        return defaultCellWidth.withExtraSize(configuration, totalColumns, hasExtra)
    }

    fun defaultRowHeaderCellWidthWithExtraSize(
        configuration: Configuration,
        totalColumns: Int,
        hasExtra: Boolean = false
    ): Dp {
        return defaultRowHeaderWidth.withExtraSize(configuration, totalColumns, hasExtra)
    }

    fun headerCellWidth(
        headerRowColumns: Int,
        configuration: Configuration,
        totalColumns: Int,
        hasTotal: Boolean = false
    ): Dp {
        val fullWidth = defaultCellWidth * totalColumns
        val rowHeaderRatio = totalColumns / headerRowColumns
        return (fullWidth / headerRowColumns)
            .withExtraSize(configuration, totalColumns, hasTotal, rowHeaderRatio)
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
        configuration: Configuration,
        totalColumns: Int,
        hasExtra: Boolean = false,
        extraWidthRatio: Int = 1
    ): Dp {
        return this + extraWidth(configuration, totalColumns, hasExtra) * extraWidthRatio
    }

    private fun extraWidth(
        configuration: Configuration,
        totalColumns: Int,
        hasTotal: Boolean
    ): Dp {
        val screenWidth = configuration.screenWidthDp
        val tableWidth = tableWidth(totalColumns, hasTotal)

        return if (tableWidth < screenWidth.dp) {
            val totalColumnCount = 1.takeIf { hasTotal } ?: 0
            val columnsCount = 1 + totalColumns + totalColumnCount
            (screenWidth.dp - tableWidth) / columnsCount
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

        return tableHorizontalPadding * 2 +
            defaultRowHeaderWidth + defaultCellWidth * totalColumns + totalCellWidth
    }
}

val LocalTableDimensions = staticCompositionLocalOf { TableDimensions() }
