package org.dhis2.form.ui.provider.inputfield

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.ui.FormView
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputSignature
import java.io.File

@Composable
fun ProvideInputSignature(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    val context = LocalContext.current

    val imageBitmap: ImageBitmap? =
        fieldUiModel.displayName?.let { path ->
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
        load = { imageBitmap },
        painterFor =
            imageBitmap?.let {
                { image ->
                    BitmapPainter(image!!)
                }
            },
        onDownloadButtonClick = {
            fieldUiModel.invokeUiEvent(UiEventType.OPEN_FILE)
        },
        onResetButtonClicked = {
            fieldUiModel.onClear()
        },
        onShareButtonClick = {
            fieldUiModel.invokeUiEvent(UiEventType.SHARE_IMAGE)
        },
        onSaveSignature = {
            it.asAndroidBitmap().let {
                val file =
                    File(
                        FileResourceDirectoryHelper.getFileResourceDirectory(context),
                        FormView.TEMP_FILE,
                    )
                file.outputStream().use { out ->
                    it.compress(Bitmap.CompressFormat.PNG, 85, out)
                    out.flush()
                }
                fieldUiModel.invokeIntent(
                    FormIntent.OnStoreFile(
                        fieldUiModel.uid,
                        file.path,
                        ValueType.IMAGE,
                    ),
                )
            }
        },
    )
}
