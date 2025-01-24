package org.dhis2.form.ui.provider.inputfield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.content.ContextCompat
import org.dhis2.commons.extensions.getBitmap
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.dialog.ImagePickerOptionsDialog
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.files.rememberCameraPicker
import org.dhis2.form.ui.files.rememberFilePicker
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputImage
import org.hisp.dhis.mobile.ui.designsystem.component.UploadState

@Composable
internal fun ProvideInputImage(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: ResourceManager,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    onFileSelected: (filePath: String) -> Unit,
) {
    var showImageOptions by remember { mutableStateOf(false) }

    var uploadState by remember(fieldUiModel) {
        mutableStateOf(
            getUploadState(
                fieldUiModel.displayName,
                fieldUiModel.isLoadingData,
            ),
        )
    }

    val painter = fieldUiModel.displayName?.getBitmap()?.let { BitmapPainter(it.asImageBitmap()) }

    val filePicker = rememberFilePicker(onFileSelected)

    val (tempFileUri, imagePicker, cameraPermission) = rememberCameraPicker(
        onSuccess = { filePath ->
            onFileSelected(filePath)
            uploadState = getUploadState(fieldUiModel.displayName, false)
        },
        onError = {
            uploadState = getUploadState(fieldUiModel.displayName, false)
            intentHandler.invoke(
                FormIntent.OnAddImageFinished(fieldUiModel.uid),
            )
        },
        onPermissionAccepted = {
            uploadState = getUploadState(fieldUiModel.displayName, true)
        },
    )

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
        onDownloadButtonClick = {
            uiEventHandler.invoke(RecyclerViewUiEvents.OpenFile(fieldUiModel))
        },
        onShareButtonClick = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_SEND,
                    fieldUiModel.displayName,
                    fieldUiModel.uid,
                ),
            )
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
            showImageOptions = true
        },
    )

    ImagePickerOptionsDialog(
        title = fieldUiModel.label,
        showImageOptions = showImageOptions,
        onDismiss = { showImageOptions = false },
        onTakePicture = { context ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                uploadState = getUploadState(fieldUiModel.displayName, true)
                imagePicker.launch(tempFileUri)
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        },
        onSelectFromGallery = {
            filePicker.launch("image/*")
        },
    )
}

internal fun getUploadState(value: String?, isLoading: Boolean): UploadState {
    return if (isLoading && value.isNullOrEmpty()) {
        UploadState.UPLOADING
    } else if (value.isNullOrEmpty()) {
        UploadState.ADD
    } else {
        UploadState.LOADED
    }
}
