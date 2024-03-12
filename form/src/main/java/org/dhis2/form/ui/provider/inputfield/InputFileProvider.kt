package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.ui.model.InputData
import org.hisp.dhis.mobile.ui.designsystem.component.InputFileResource
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import java.io.File

@Composable
internal fun ProvideInputFileResource(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: ResourceManager,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    var uploadState by remember(fieldUiModel) { mutableStateOf(getFileUploadState(fieldUiModel.displayName, fieldUiModel.isLoadingData)) }

    val fileInputData =
        fieldUiModel.displayName?.let {
            val file = File(it)
            InputData.FileInputData(
                fileName = file.name,
                fileSize = file.length(),
                filePath = file.path,
            )
        }

    InputFileResource(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        inputShellState = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        buttonText = resources.getString(R.string.add_file),
        uploadFileState = uploadState,
        fileName = fileInputData?.fileName,
        fileWeight = fileInputData?.fileSizeLabel,
        onSelectFile = {
            uploadState = getFileUploadState(fieldUiModel.displayName, true)
            fieldUiModel.invokeUiEvent(UiEventType.ADD_FILE)
        },
        onClear = { fieldUiModel.onClear() },
        onUploadFile = {
            uploadState = getFileUploadState(fieldUiModel.displayName, false)
            uiEventHandler.invoke(RecyclerViewUiEvents.OpenFile(fieldUiModel))
        },
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
    )
}

private fun getFileUploadState(value: String?, isLoading: Boolean): UploadFileState {
    return if (isLoading && value.isNullOrEmpty()) {
        UploadFileState.UPLOADING
    } else if (value.isNullOrEmpty()) {
        UploadFileState.ADD
    } else {
        UploadFileState.LOADED
    }
}
