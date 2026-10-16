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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.files.rememberCameraPicker
import org.dhis2.form.ui.files.rememberFilePicker
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.mobile.commons.ui.ImagePickerOptionsDialog
import org.hisp.dhis.mobile.ui.designsystem.component.InputImage
import org.hisp.dhis.mobile.ui.designsystem.component.UploadState

@Composable
internal fun ProvideInputImage(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
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

    val filePicker = rememberFilePicker(onFileSelected)

    val cameraPicker =
        rememberCameraPicker(
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
        imageFilePath = fieldUiModel.displayName,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        addImageBtnText = stringResource(R.string.add_image),
        isRequired = fieldUiModel.mandatory,
        uploadState = uploadState,
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

    val context = LocalContext.current

    ImagePickerOptionsDialog(
        title = fieldUiModel.label,
        showImageOptions = showImageOptions,
        cameraButtonLabel = stringResource(R.string.take_photo),
        galleryButtonLabel = stringResource(R.string.from_gallery_v2),
        onDismiss = { showImageOptions = false },
        onTakePicture = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                uploadState = getUploadState(fieldUiModel.displayName, true)
                cameraPicker.takePicture()
            } else {
                cameraPicker.requestCameraPermission()
            }
        },
        onSelectFromGallery = {
            filePicker.launch("image/*")
        },
    )
}

internal fun getUploadState(
    value: String?,
    isLoading: Boolean,
): UploadState =
    if (isLoading && value.isNullOrEmpty()) {
        UploadState.UPLOADING
    } else if (value.isNullOrEmpty()) {
        UploadState.ADD
    } else {
        UploadState.LOADED
    }
