package org.dhis2.tracker.ui.input.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.mobile.commons.dates.calculateAgeFromDate
import org.dhis2.mobile.commons.dates.calculateDateFromAge
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.age
import org.dhis2.mobile.commons.resources.age_or
import org.dhis2.mobile.commons.resources.cancel
import org.dhis2.mobile.commons.resources.date_of_birth
import org.dhis2.mobile.commons.resources.ok
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.inputState
import org.dhis2.tracker.ui.input.model.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.InputAge
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputAgeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputAgeState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProvideTrackerAgeInput(
    model: TrackerInputModel,
    inputStyle: InputStyle,
    onNextClicked: () -> Unit,
    modifier: Modifier,
    onValueChange: (String?) -> Unit,
) {
    var inputType by remember {
        mutableStateOf(
            if (!model.value.isNullOrEmpty()) {
                model.value.let {
                    AgeInputType.DateOfBirth(TextFieldValue(it, TextRange(it.length)))
                }
            } else {
                AgeInputType.None
            },
        )
    }

    DisposableEffect(model.value) {
        when (inputType) {
            is AgeInputType.Age ->
                if (!model.value.isNullOrEmpty()) {
                    calculateAgeFromDate(
                        model.value,
                        (inputType as AgeInputType.Age).unit.name,
                    )?.let {
                        (inputType as AgeInputType.Age).copy(
                            value =
                                TextFieldValue(
                                    it,
                                    TextRange(it.length),
                                ),
                        )
                    } ?: AgeInputType.None
                }

            is AgeInputType.DateOfBirth ->
                if (!model.value.isNullOrEmpty()) {
                    model.value.let {
                        (inputType as AgeInputType.DateOfBirth).copy(
                            value =
                                TextFieldValue(
                                    it,
                                    TextRange(it.length),
                                ),
                        )
                    }
                }

            AgeInputType.None -> {
                // no-op
            }
        }

        onDispose { }
    }

    InputAge(
        state =
            rememberInputAgeState(
                inputAgeData =
                    InputAgeData(
                        title = model.label,
                        inputStyle = inputStyle,
                        isRequired = model.mandatory,
                        dateOfBirthLabel = stringResource(Res.string.date_of_birth),
                        orLabel = stringResource(Res.string.age_or),
                        ageLabel = stringResource(Res.string.age),
                        cancelText = stringResource(Res.string.cancel),
                        acceptText = stringResource(Res.string.ok),
                    ),
                inputType = inputType,
                inputState = model.inputState(),
                legendData = model.legend,
                supportingText = model.supportingText(),
            ),
        onValueChanged = { ageInputType ->
            if (ageInputType != null) {
                inputType = ageInputType
            }

            when (val type = inputType) {
                is AgeInputType.Age -> {
                    calculateDateFromAge(type.value.text, type.unit.name)?.let { calculatedDate ->
                        onValueChange(calculatedDate)
                    }
                }

                is AgeInputType.DateOfBirth -> {
                    type.value.text
                        .takeIf { it != model.value }
                        ?.let {
                            onValueChange(it)
                        }
                }

                AgeInputType.None -> {
                    onValueChange(null)
                }
            }
        },
        onImeActionClick = { _ ->
            onNextClicked()
        },
        modifier = modifier,
    )
}
