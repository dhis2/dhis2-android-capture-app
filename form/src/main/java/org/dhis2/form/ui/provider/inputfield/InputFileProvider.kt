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
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.files.rememberFilePicker
import org.hisp.dhis.mobile.ui.designsystem.component.InputFileResource
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import java.io.File
import java.text.DecimalFormat

@Composable
internal fun ProvideInputFileResource(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: ResourceManager,
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
    val file = fieldUiModel.displayName?.let { File(it) }

    val filePicker = rememberFilePicker(onFileSelected)

    InputFileResource(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        inputShellState = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        buttonText = resources.getString(R.string.add_file),
        uploadFileState = uploadState,
        fileName = file?.name,
        fileWeight = file?.length()?.let { fileSizeLabel(it) },
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

private fun fileSizeLabel(fileSize: Long) =
    run {
        val kb = fileSize / 1024f
        val mb = kb / 1024f
        if (kb < 1024f) {
            "${DecimalFormat("*0").format(kb)}KB"
        } else {
            "${DecimalFormat("*0.##").format(mb)}MB"
        }
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
