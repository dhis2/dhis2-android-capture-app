package org.dhis2.mobile.aggregates.model.mapper

import androidx.compose.ui.graphics.Color
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.ui.states.InputData
import org.dhis2.mobile.commons.extensions.toColor
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

internal fun CellInfo.toInputData(cellId: String) =
    InputData(
        id = cellId,
        label = label,
        value = value,
        inputType = inputType,
        inputShellState = InputShellState.UNFOCUSED,
        inputExtra = inputExtra,
        supportingText = supportingText.map { text ->
            SupportingTextData(
                text = text,
                state = SupportingTextState.DEFAULT,
            )
        } + errors.map { error ->
            SupportingTextData(
                text = error,
                state = SupportingTextState.ERROR,
            )
        } + warnings.map { warning ->
            SupportingTextData(
                text = warning,
                state = SupportingTextState.WARNING,
            )
        },
        legendData = LegendData(
            color = legendColor?.toColor() ?: Color.Unspecified,
            title = legendLabel ?: "",
        ),
        isRequired = isRequired,
    )
