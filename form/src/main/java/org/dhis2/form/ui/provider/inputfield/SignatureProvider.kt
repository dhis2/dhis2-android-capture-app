package org.dhis2.form.ui.provider.inputfield

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputSignature
import java.io.File

@Composable
fun ProvideInputSignature(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    val imageBitmap: ImageBitmap? = fieldUiModel.displayName?.let { path ->
        File(path)
            .takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }
            ?.asImageBitmap()
    }

    var uploadState by remember(fieldUiModel) { mutableStateOf(getUploadState(fieldUiModel.displayName, fieldUiModel.isLoadingData)) }

    InputSignature(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        uploadState = uploadState,
        load = { imageBitmap },
        painterFor = imageBitmap?.let {
            {
                    image ->
                BitmapPainter(image!!)
            }
        },
        onDownloadButtonClick = {
            uiEventHandler.invoke(RecyclerViewUiEvents.OpenFile(fieldUiModel))
        },
        onResetButtonClicked = {
            fieldUiModel.onClear()
            uploadState = getUploadState(fieldUiModel.displayName, false)
            intentHandler.invoke(
                FormIntent.OnAddImageFinished(
                    uid = fieldUiModel.uid,
                ),
            )
        },
        onAddButtonClicked = {
            uploadState = getUploadState(fieldUiModel.displayName, true)
            fieldUiModel.invokeUiEvent(UiEventType.ADD_SIGNATURE)
        },
        onImageClick = {
            uiEventHandler.invoke(RecyclerViewUiEvents.ShowImage(fieldUiModel.label, fieldUiModel.displayName ?: ""))
        },
    )
}
