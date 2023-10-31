package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.ui.binding.getBitmap
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputImage
import org.hisp.dhis.mobile.ui.designsystem.component.UploadState

@Composable
internal fun ProvideInputImage(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: ResourceManager,
    intentHandler: (FormIntent) -> Unit,
) {
    var uploadState by remember(fieldUiModel) { mutableStateOf(getUploadState(fieldUiModel, fieldUiModel.isLoadingData)) }

    val painter = fieldUiModel.displayName?.getBitmap()?.let { BitmapPainter(it.asImageBitmap()) }
    InputImage(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        addImageBtnText = resources.getString(R.string.add_image),
        isRequired = fieldUiModel.mandatory,
        uploadState = uploadState,
        painterFor = { remember { it!! } },
        load = {
            painter
        },
        onDownloadButtonClick = { fieldUiModel.invokeUiEvent(UiEventType.OPEN_FILE) },
        onResetButtonClicked = {
            fieldUiModel.onClear()
            uploadState = getUploadState(fieldUiModel, false)
            intentHandler.invoke(
                FormIntent.OnAddImageFinished(
                    uid = fieldUiModel.uid,
                ),
            )
        },
        onAddButtonClicked = {
            uploadState = getUploadState(fieldUiModel, true)
            fieldUiModel.invokeUiEvent(UiEventType.ADD_PICTURE)
        },
        onImageClick = {
            fieldUiModel.invokeUiEvent(UiEventType.SHOW_PICTURE)
        },
    )
}

private fun getUploadState(fieldUiModel: FieldUiModel, isLoading: Boolean): UploadState {
    return if (isLoading && fieldUiModel.displayName.isNullOrEmpty()) {
        UploadState.UPLOADING
    } else if (fieldUiModel.displayName.isNullOrEmpty()) {
        UploadState.ADD
    } else {
        UploadState.LOADED
    }
}
