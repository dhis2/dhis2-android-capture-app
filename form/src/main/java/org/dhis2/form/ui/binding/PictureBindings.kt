package org.dhis2.form.ui.binding

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.databinding.BindingAdapter
import org.dhis2.commons.extensions.getBitmap
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.ui.inputs.AddButtonData
import org.dhis2.ui.inputs.FormInputBox
import org.dhis2.ui.inputs.PictureInput
import org.dhis2.ui.theme.Dhis2Theme
import org.dhis2.ui.theme.errorColor
import org.dhis2.ui.theme.textSecondary
import org.dhis2.ui.theme.warningColor

@BindingAdapter("picture")
fun ComposeView.setPicture(fieldUiModel: FieldUiModel) {
    setContent {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        Dhis2Theme {
            FormInputBox(
                labelText = fieldUiModel.label,
                helperText = fieldUiModel.error ?: fieldUiModel.warning,
                descriptionText = fieldUiModel.description,
                selected = fieldUiModel.focused,
                enabled = fieldUiModel.editable,
                labelTextColor = textSecondary,
                helperTextColor = when {
                    fieldUiModel.error != null -> errorColor
                    fieldUiModel.warning != null -> warningColor
                    else -> textSecondary
                },
            ) {
                PictureInput(
                    imageValue = fieldUiModel.displayName?.getBitmap(),
                    enabled = fieldUiModel.editable,
                    addButtonData =
                    when (fieldUiModel.renderingType != UiRenderType.CANVAS) {
                        true -> AddButtonData(
                            onClick = { fieldUiModel.invokeUiEvent(UiEventType.ADD_PICTURE) },
                            icon = painterResource(id = R.drawable.ic_add_image),
                            label = stringResource(id = R.string.add_image),
                        )
                        false -> AddButtonData(
                            onClick = { fieldUiModel.invokeUiEvent(UiEventType.ADD_SIGNATURE) },
                            icon = painterResource(id = R.drawable.ic_signature),
                            label = stringResource(id = R.string.add_signature),
                        )
                    },
                    onClick = { fieldUiModel.invokeUiEvent(UiEventType.SHOW_PICTURE) },
                    onClear = { fieldUiModel.onClear() },
                )
            }
        }
    }
}
