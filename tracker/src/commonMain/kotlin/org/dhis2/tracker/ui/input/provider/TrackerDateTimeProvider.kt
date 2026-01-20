package org.dhis2.tracker.ui.input.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.inputState
import org.dhis2.tracker.ui.input.model.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputDateTimeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputDateTimeState

@Composable
fun ProvideTrackerDateTimeInput(
    model: TrackerInputModel,
    inputStyle: InputStyle,
    onNextClicked: () -> Unit,
    modifier: Modifier,
) {
    val (actionType, visualTransformation) =
        when (model.valueType) {
            TrackerInputType.DATE_TIME -> DateTimeActionType.DATE_TIME to DateTimeTransformation()
            TrackerInputType.TIME -> DateTimeActionType.TIME to TimeTransformation()
            else -> DateTimeActionType.DATE to DateTransformation()
        }
    val textSelection =
        TextRange(
            model.value?.length ?: 0,
        )

    val yearIntRange = getYearRange()
    val selectableDates =
        SelectableDates(
            initialDate = DEFAULT_MIN_DATE,
            endDate = DEFAULT_MAX_DATE,
        )

    var value by remember(model.value) {
        mutableStateOf(
            model.value?.let { value ->
                TextFieldValue(
                    value,
                    textSelection,
                )
            } ?: TextFieldValue(),
        )
    }

    val inputState =
        rememberInputDateTimeState(
            InputDateTimeData(
                title = model.label,
                actionType = actionType,
                visualTransformation = visualTransformation,
                isRequired = model.mandatory,
                selectableDates = selectableDates,
                yearRange = yearIntRange,
                inputStyle = inputStyle,
            ),
            inputTextFieldValue = value,
            inputState = model.inputState(),
            legendData = model.legend,
            supportingText = model.supportingText(),
        )

    InputDateTime(
        state = inputState,
        modifier =
            modifier.semantics {
                contentDescription = value.text
            },
        onValueChanged = {
            value = it ?: TextFieldValue()
            model.onValueChange(value.text.ifEmpty { null })
        },
        onImeActionClick = { imeAction ->
            onNextClicked()
        },
    )
}

private fun getYearRange(): IntRange =
    IntRange(
        DEFAULT_MIN_DATE
            .substring(4, 8)
            .toInt(),
        DEFAULT_MAX_DATE
            .substring(4, 8)
            .toInt(),
    )

const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
