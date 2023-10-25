package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.hisp.dhis.mobile.ui.designsystem.component.InputPolygon

@Composable
fun ProvidePolygon(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    InputPolygon(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        polygonAdded = !fieldUiModel.value.isNullOrEmpty(),
        isRequired = fieldUiModel.mandatory,
        onResetButtonClicked = { fieldUiModel.onClear() },
        onUpdateButtonClicked = { fieldUiModel.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP) },
    )
}
