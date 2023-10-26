package org.dhis2.form.extensions

import androidx.compose.ui.graphics.Color
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

fun FieldUiModel.supportingText() = listOfNotNull(
    error?.let {
        SupportingTextData(
            it,
            SupportingTextState.ERROR,
        )
    },
    warning?.let {
        SupportingTextData(
            it,
            SupportingTextState.WARNING,
        )
    },
    description?.let {
        SupportingTextData(
            it,
            SupportingTextState.DEFAULT,
        )
    },
).ifEmpty { null }

fun FieldUiModel.legend() = legend?.let {
    LegendData(Color(it.color), it.label ?: "", null)
}

fun FieldUiModel.orientation() = when (renderingType) {
    UiRenderType.VERTICAL_RADIOBUTTONS,
    UiRenderType.VERTICAL_CHECKBOXES,
    -> {
        Orientation.VERTICAL
    }

    UiRenderType.HORIZONTAL_RADIOBUTTONS,
    UiRenderType.HORIZONTAL_CHECKBOXES,
    -> {
        Orientation.HORIZONTAL
    }

    else -> Orientation.HORIZONTAL
}

fun FieldUiModel.inputState() = when {
    error != null -> InputShellState.ERROR
    !editable -> InputShellState.DISABLED
    focused -> InputShellState.FOCUSED
    else -> InputShellState.UNFOCUSED
}

fun FieldUiModel.autocompleteList() = when (renderingType) {
    UiRenderType.AUTOCOMPLETE -> {
        autocompleteList
    }
    else -> null
}
