package org.dhis2.composetable.ui

import androidx.compose.material.contentColorFor
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
        textColor = contentColorFor(LocalTableColors.current.primary)
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
        contentColorFor(TableTheme.colors.primary)
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

@Composable
fun styleForCell(
    isSelected: Boolean,
    isParentSelected: Boolean,
    hasError: Boolean,
    hasWarning: Boolean,
    isEditable: Boolean,
    legendColor: Int?
) = CellStyle.CellBorderStyle(
    borderColor = when {
        isSelected && hasError -> TableTheme.colors.errorColor
        isSelected && hasWarning -> TableTheme.colors.warningColor
        isSelected -> TableTheme.colors.primary
        else -> Color.Transparent
    },
    backgroundColor = when {
        legendColor != null -> Color.Transparent
        !isEditable -> TableTheme.colors.disabledCellBackground
        else -> when {
            isSelected -> TableTheme.colors.tableBackground
            isParentSelected -> TableTheme.colors.primaryLight
            else -> TableTheme.colors.tableBackground
        }
    }
)
