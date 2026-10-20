package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.files.rememberFilePicker
import org.dhis2.mobile.commons.extensions.fileResourcePath
import org.dhis2.mobile.commons.extensions.getFormattedFileSize
import org.hisp.dhis.mobile.ui.designsystem.component.InputFileResource
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState

@Composable
internal fun ProvideInputFileResource(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    onFileSelected: (filePath: String) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    var uploadState by remember(fieldUiModel) {
        mutableStateOf(
            getFileUploadState(
                fieldUiModel.displayName,
                fieldUiModel.isLoadingData,
            ),
        )
    }
    val fileWeight by produceState<String?>(null, fieldUiModel.value) {
        value =
            withContext(Dispatchers.IO) {
                fieldUiModel.value?.fileResourcePath()?.let { getFormattedFileSize(it) }
            }
    }

    val filePicker = rememberFilePicker(onFileSelected)

    InputFileResource(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        inputShellState = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        buttonText = stringResource(R.string.add_file),
        uploadFileState = uploadState,
        fileName = fieldUiModel.displayName,
        fileWeight = fileWeight,
        onSelectFile = {
            uploadState = getFileUploadState(fieldUiModel.displayName, true)
            filePicker.launch("*/*")
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

private fun getFileUploadState(
    value: String?,
    isLoading: Boolean,
): UploadFileState =
    if (isLoading && value.isNullOrEmpty()) {
        UploadFileState.UPLOADING
    } else if (value.isNullOrEmpty()) {
        UploadFileState.ADD
    } else {
        UploadFileState.LOADED
    }
