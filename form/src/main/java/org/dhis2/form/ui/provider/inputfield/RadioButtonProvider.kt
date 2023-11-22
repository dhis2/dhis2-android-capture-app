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
import org.hisp.dhis.mobile.ui.designsystem.component.InputRadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData

@Composable
internal fun ProvideRadioButtonInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusRequester: FocusRequester,
) {
    val data = fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.map { option ->
        RadioButtonData(
            uid = option.uid(),
            selected = fieldUiModel.displayName == option.displayName(),
            enabled = true,
            textInput = option.displayName(),
        )
    } ?: emptyList()

    InputRadioButton(
        modifier = modifier,
        title = fieldUiModel.label,
        radioButtonData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            focusRequester.requestFocus()
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    fieldUiModel.optionSetConfiguration?.optionsToDisplay()
                        ?.find { it.uid() == item?.uid }?.code(),
                    fieldUiModel.valueType,
                ),
            )
        },
    )
}

@Composable
internal fun ProvideYesNoRadioButtonInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
    focusRequester: FocusRequester,
) {
    val data = listOf(
        RadioButtonData(
            uid = "true",
            selected = fieldUiModel.isAffirmativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.yes),
        ),
        RadioButtonData(
            uid = "false",
            selected = fieldUiModel.isNegativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.no),
        ),
    )

    InputRadioButton(
        modifier = modifier,
        title = fieldUiModel.label,
        radioButtonData = data,
        orientation = fieldUiModel.orientation(),
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            focusRequester.requestFocus()
            when (item?.uid) {
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

                else -> {
                    intentHandler(
                        FormIntent.ClearValue(fieldUiModel.uid),
                    )
                }
            }
        },
    )
}
