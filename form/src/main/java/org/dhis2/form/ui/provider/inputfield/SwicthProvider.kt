package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch

@Composable
internal fun ProvideYesOnlySwitchInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    InputYesOnlySwitch(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        isChecked = fieldUiModel.isAffirmativeChecked,
        onClick = {
            fieldUiModel.onItemClick()
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
