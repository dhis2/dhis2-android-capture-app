package org.dhis2.form.ui.provider.inputfield

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.hisp.dhis.mobile.ui.designsystem.component.InputSignature
import org.hisp.dhis.mobile.ui.designsystem.component.UploadState
import java.io.File

@Composable
fun ProvideInputSignature(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    val imageBitmap: ImageBitmap? = fieldUiModel.displayName?.let { path ->
        File(path)
            .takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }
            ?.asImageBitmap()
    }

    InputSignature(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        uploadState = if (fieldUiModel.value != null) UploadState.LOADED else UploadState.ADD,
        load = { imageBitmap },
        painterFor = imageBitmap?.let {
            {
                    image ->
                BitmapPainter(image!!)
            }
        },
        onDownloadButtonClick = { fieldUiModel.invokeUiEvent(UiEventType.SHOW_PICTURE) },
        onResetButtonClicked = { fieldUiModel.onClear() },
        onAddButtonClicked = { fieldUiModel.invokeUiEvent(UiEventType.ADD_SIGNATURE) },
        onImageClick = {}
    )
}
