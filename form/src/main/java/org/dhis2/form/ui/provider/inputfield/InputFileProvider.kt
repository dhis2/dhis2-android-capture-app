package org.dhis2.form.ui.provider.inputfield

import android.content.res.Resources
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.ui.model.InputData
import org.hisp.dhis.mobile.ui.designsystem.component.InputFileResource
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import java.io.File

@Composable
internal fun ProvideInputFileResource(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: Resources,
) {
    val uploadState by remember(fieldUiModel) { mutableStateOf(getUploadState(fieldUiModel)) }

    val fileInputData = fieldUiModel.value?.let {
        val file = File(it)
        InputData.FileInputData(
            fileName = file.name,
            fileSize = file.length(),
            filePath = file.path,
        )
    }

    val filename by remember(fieldUiModel) {
        mutableStateOf(fileInputData?.fileName)
    }

    val fileSize by remember(fieldUiModel) {
        mutableStateOf(fileInputData?.fileSizeLabel)
    }
    InputFileResource(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        inputShellState = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        buttonText = resources.getString(R.string.add_file),
        uploadFileState = uploadState,
        fileName = filename,
        fileWeight = fileSize,
        onSelectFile = {
            fieldUiModel.invokeUiEvent(UiEventType.ADD_FILE)
        },
        onClear = { fieldUiModel.onClear() },
        onUploadFile = {
            fieldUiModel.invokeUiEvent(UiEventType.OPEN_FILE)
        },
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
    )
}

private fun getUploadState(fieldUiModel: FieldUiModel): UploadFileState {
    return if (fieldUiModel.displayName.isNullOrEmpty()) {
        UploadFileState.ADD
    } else {
        UploadFileState.LOADED
    }
}
