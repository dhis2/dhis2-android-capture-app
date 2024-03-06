package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed class CellStyle {
    data class HeaderStyle(val backgroundColor: Color, val textColor: Color) : CellStyle()
    data class CellBorderStyle(val backgroundColor: Color, val borderColor: Color) : CellStyle()

    fun backgroundColor() = when (this) {
        is CellBorderStyle -> backgroundColor
        is HeaderStyle -> backgroundColor
    }

    fun mainColor() = when (this) {
        is CellBorderStyle -> borderColor
        is HeaderStyle -> textColor
    }
}

@Composable
fun styleForColumnHeader(
    isSelected: Boolean,
    isParentSelected: Boolean,
    columnIndex: Int
): CellStyle = when {
    isSelected -> CellStyle.HeaderStyle(
        backgroundColor = LocalTableColors.current.primary,
        textColor = LocalTableColors.current.onPrimary
    )
    isParentSelected -> CellStyle.HeaderStyle(
        backgroundColor = LocalTableColors.current.primaryLight,
        textColor = LocalTableColors.current.headerText
    )
    columnIndex % 2 == 0 -> CellStyle.HeaderStyle(
        backgroundColor = LocalTableColors.current.headerBackground1,
        textColor = LocalTableColors.current.headerText
    )
    else ->
        CellStyle.HeaderStyle(
            backgroundColor = LocalTableColors.current.headerBackground2,
            textColor = LocalTableColors.current.headerText
        )
}

@Composable
fun styleForRowHeader(isSelected: Boolean, isOtherRowSelected: Boolean): CellStyle = when {
    isSelected -> CellStyle.HeaderStyle(
        TableTheme.colors.primary,
        TableTheme.colors.onPrimary
    )
    isOtherRowSelected -> CellStyle.HeaderStyle(
        TableTheme.colors.primaryLight,
        TableTheme.colors.primary
    )
    else -> CellStyle.HeaderStyle(
        backgroundColor = TableTheme.colors.tableBackground,
        textColor = TableTheme.colors.primary
    )
}

fun styleForCell(
    tableColorProvider: () -> TableColors,
    isSelected: Boolean,
    isParentSelected: Boolean,
    hasError: Boolean,
    hasWarning: Boolean,
    isEditable: Boolean,
    legendColor: Int?
) = CellStyle.CellBorderStyle(
    borderColor = when {
        isSelected && hasError -> tableColorProvider().errorColor
        isSelected && hasWarning -> tableColorProvider().warningColor
        isSelected -> tableColorProvider().primary
        else -> Color.Transparent
    },
    backgroundColor = when {
        legendColor != null -> Color.Transparent
        !isEditable && isParentSelected -> tableColorProvider().disabledSelectedBackground
        isParentSelected -> tableColorProvider().primaryLight
        !isEditable -> tableColorProvider().disabledCellBackground
        isSelected -> tableColorProvider().tableBackground
        else -> tableColorProvider().tableBackground
    }
)
