package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch

@Composable
internal fun ProvideYesOnlySwitchInput(
    fieldUiModel: FieldUiModel,
) {
    InputYesOnlySwitch(
        title = fieldUiModel.label,
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        isChecked = fieldUiModel.isAffirmativeChecked,
        onClick = {
            if (!fieldUiModel.isAffirmativeChecked) {
                fieldUiModel.onSaveBoolean(true)
            } else {
                fieldUiModel.onClear()
            }
        },
    )
}
