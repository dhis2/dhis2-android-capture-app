package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel

@Composable
fun provideParameterSelectorItem(
    resources: ResourceManager,
    focusManager: FocusManager,
    fieldUiModel: FieldUiModel,
    callback: FieldUiModel.Callback,
): ParameterSelectorItemModel {
    val status by remember(fieldUiModel.focused) {
        mutableStateOf(
            if (fieldUiModel.focused) {
                ParameterSelectorItemModel.Status.FOCUSED
            } else if (fieldUiModel.value.isNullOrEmpty()) {
                ParameterSelectorItemModel.Status.CLOSED
            } else {
                ParameterSelectorItemModel.Status.UNFOCUSED
            },
        )
    }

    return ParameterSelectorItemModel(
        icon = provideIcon(fieldUiModel.valueType, fieldUiModel.renderingType),
        label = fieldUiModel.label,
        helper = resources.getString(R.string.optional),
        inputField = {
            FieldProvider(
                modifier = Modifier,
                inputStyle = InputStyle.ParameterInputStyle(),
                fieldUiModel = fieldUiModel,
                uiEventHandler = callback::recyclerViewUiEvents,
                intentHandler = callback::intent,
                resources = resources,
                focusManager = focusManager,
                onNextClicked = { focusManager.moveFocus(FocusDirection.Down) },
            )
        },
        status = status,
        onExpand = {
            fieldUiModel.onItemClick()
        },
    )
}

private fun provideIcon(valueType: ValueType?, renderingType: UiRenderType?): ImageVector =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> {
                    Icons.Outlined.QrCode2
                }
                UiRenderType.BAR_CODE -> {
                    Icons.Outlined.QrCode
                }
                else -> {
                    Icons.Outlined.AddCircleOutline
                }
            }
        }
        else -> Icons.Outlined.AddCircleOutline
    }
