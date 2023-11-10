package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.orientation
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlyCheckBox

@Composable
internal fun ProvideCheckBoxInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusRequester: FocusRequester,
) {
    val data = fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.map { option ->
        CheckBoxData(
            uid = option.uid(),
            checked = fieldUiModel.displayName == option.displayName(),
            enabled = true,
            textInput = option.displayName(),
        )
    } ?: emptyList()

    InputCheckBox(
        modifier = modifier,
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            focusRequester.requestFocus()
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    fieldUiModel.optionSetConfiguration?.optionsToDisplay()
                        ?.find { it.uid() == item.uid }?.code(),
                    fieldUiModel.valueType,
                ),
            )
        },
        onClearSelection = {
            focusRequester.requestFocus()
            intentHandler(
                FormIntent.ClearValue(fieldUiModel.uid),
            )
        },
    )
}

@Composable
internal fun ProvideYesNoCheckBoxInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
    focusRequester: FocusRequester,
) {
    val data = listOf(
        CheckBoxData(
            uid = "true",
            checked = fieldUiModel.isAffirmativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.yes),
        ),
        CheckBoxData(
            uid = "false",
            checked = fieldUiModel.isNegativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.no),
        ),
    )

    InputCheckBox(
        modifier = modifier,
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            focusRequester.requestFocus()
            when (item.uid) {
                "true" -> {
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            true.toString(),
                            fieldUiModel.valueType,
                        ),
                    )
                }

                "false" -> {
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            false.toString(),
                            fieldUiModel.valueType,
                        ),
                    )
                }

                else -> fieldUiModel.onClear()
            }
        },
        onClearSelection = {
            focusRequester.requestFocus()
            intentHandler(
                FormIntent.ClearValue(fieldUiModel.uid),
            )
        },
    )
}

@Composable
internal fun ProvideYesOnlyCheckBoxInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusRequester: FocusRequester,
) {
    val cbData = CheckBoxData(
        uid = "",
        checked = fieldUiModel.isAffirmativeChecked,
        enabled = true,
        textInput = fieldUiModel.label,
    )

    InputYesOnlyCheckBox(
        modifier = modifier,
        checkBoxData = cbData,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onClick = {
            focusRequester.requestFocus()
            if (!fieldUiModel.isAffirmativeChecked) {
                intentHandler(
                    FormIntent.OnSave(
                        fieldUiModel.uid,
                        true.toString(),
                        fieldUiModel.valueType,
                    ),
                )
            } else {
                intentHandler(
                    FormIntent.ClearValue(fieldUiModel.uid),
                )
            }
        },
    )
}
