package org.dhis2.composetable.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import org.dhis2.composetable.model.TableCell

data class TableCellUiOptions(val cellValue: TableCell, val value: String) {

    val backgroundColor = when (cellValue.editable) {
        false -> DisabledCellBackground
        else -> Color.Transparent
    }

    val textColor = when {
        cellValue.error != null -> ErrorColor
        cellValue.editable == false -> DisabledCellText
        else -> CellText
    }

    val mandatoryColor = when {
        value.isNotEmpty() -> Color.LightGray
        else -> ErrorColor
    }
    val mandatoryAlignment = when {
        value.isNotEmpty() -> Alignment.TopStart
        else -> Alignment.CenterEnd
    }

    val enabled = cellValue.editable == true

    fun borderColor(focusState: FocusState, primary: Color) = when {
        focusState.isFocused -> when {
            cellValue.error != null -> ErrorColor
            else -> primary
        }
        else -> Color.Transparent
    }
}
