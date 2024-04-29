package org.dhis2.form.ui.binding

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.toColorInt
import androidx.databinding.BindingAdapter
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.ui.inputs.FileInput
import org.dhis2.ui.inputs.FormInputBox
import org.dhis2.ui.model.InputData
import org.dhis2.ui.theme.Dhis2Theme
import java.io.File

@BindingAdapter("add_file")
fun ComposeView.addFile(fieldUiModel: FieldUiModel) {
    setContent {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        Dhis2Theme {
            FormInputBox(
                labelText = fieldUiModel.formattedLabel,
                helperText = fieldUiModel.error ?: fieldUiModel.warning,
                descriptionText = fieldUiModel.description,
                selected = fieldUiModel.focused,
                enabled = fieldUiModel.editable,
                labelTextColor = Color.Black.copy(alpha = 0.54f),
                helperTextColor = when {
                    fieldUiModel.error != null -> Color("#E91E63".toColorInt())
                    fieldUiModel.warning != null -> Color("#FF9800".toColorInt())
                    else -> Color.Black.copy(alpha = 0.38f)
                },
            ) {
                val fileInputData = fieldUiModel.value?.let {
                    val file = File(it)
                    InputData.FileInputData(
                        fileName = file.name,
                        fileSize = file.length(),
                        filePath = file.path,
                    )
                }
                FileInput(
                    fileInputData = fileInputData,
                    addFileLabel = stringResource(id = R.string.add_file),
                    enabled = fieldUiModel.editable,
                    onAddFile = {
                        fieldUiModel.invokeUiEvent(UiEventType.ADD_FILE)
                    },
                    onDeleteFile = {
                        fieldUiModel.onClear()
                    },
                    onDownloadClick = {
                        fieldUiModel.invokeUiEvent(UiEventType.OPEN_FILE)
                    },
                )
            }
        }
    }
}
