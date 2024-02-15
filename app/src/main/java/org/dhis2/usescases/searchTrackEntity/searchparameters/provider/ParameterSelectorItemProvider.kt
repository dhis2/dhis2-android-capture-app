package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun provideParameterSelectorItem(
    resources: ResourceManager,
    focusManager: FocusManager,
    fieldUiModel: FieldUiModel,
    callback: FieldUiModel.Callback,
): ParameterSelectorItemModel {
    val focusRequester = remember { FocusRequester() }

    val status = if (fieldUiModel.focused) {
        ParameterSelectorItemModel.Status.FOCUSED
    } else if (fieldUiModel.value.isNullOrEmpty()) {
        ParameterSelectorItemModel.Status.CLOSED
    } else {
        ParameterSelectorItemModel.Status.UNFOCUSED
    }

    LaunchedEffect(key1 = status) {
        if (status == ParameterSelectorItemModel.Status.FOCUSED) {
            focusRequester.requestFocus()
        }
    }

    return ParameterSelectorItemModel(
        icon = { ProvideIcon(fieldUiModel.valueType, fieldUiModel.renderingType) },
        label = fieldUiModel.label,
        helper = resources.getString(R.string.optional),
        inputField = {
            FieldProvider(
                modifier = Modifier
                    .focusRequester(focusRequester),
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

@Composable
private fun ProvideIcon(valueType: ValueType?, renderingType: UiRenderType?) =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> {
                    Icon(
                        imageVector = Icons.Outlined.QrCode2,
                        contentDescription = "Icon Button",
                        tint = SurfaceColor.Primary,
                    )
                }

                UiRenderType.BAR_CODE -> {
                    Icon(
                        painter = provideDHIS2Icon("material_barcode_scanner"),
                        contentDescription = "Icon Button",
                        tint = SurfaceColor.Primary,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Icon Button",
                        tint = SurfaceColor.Primary,
                    )
                }
            }
        }

        else -> Icon(
            imageVector = Icons.Outlined.AddCircleOutline,
            contentDescription = "Icon Button",
            tint = SurfaceColor.Primary,
        )
    }
