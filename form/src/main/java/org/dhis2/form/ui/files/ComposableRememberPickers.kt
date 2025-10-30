package org.dhis2.form.ui.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.dhis2.commons.bindings.rotateImage
import org.dhis2.commons.data.FormFileProvider
import org.dhis2.mobile.commons.files.GetFileResource
import org.dhis2.mobile.commons.files.toFileOverWrite
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import java.io.File

@Composable
fun rememberFilePicker(onResult: (String) -> Unit) =
    with(LocalContext.current) {
        val launcher =
            rememberLauncherForActivityResult(
                contract = GetFileResource(),
                onResult = { uris ->
                    uris
                        .firstOrNull()
                        ?.toFileOverWrite(context = this)
                        ?.path
                        ?.let(onResult)
                },
            )
        return@with launcher
    }

@Composable
fun rememberCameraPicker(
    onSuccess: (String) -> Unit,
    onError: () -> Unit,
    onPermissionAccepted: () -> Unit,
) = with(
    LocalContext.current,
) {
    val tempFile =
        File(
            FileResourceDirectoryHelper.getFileResourceDirectory(this),
            "tempFile.png",
        )

    val photoUri =
        FileProvider.getUriForFile(
            this,
            FormFileProvider.fileProviderAuthority,
            tempFile,
        )

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    onSuccess(tempFile.rotateImage(this).path)
                } else {
                    onError()
                }
            },
        )
    val cameraPermission =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { accepted ->
            if (accepted) {
                onPermissionAccepted()
                launcher.launch(photoUri)
            } else {
                onError()
            }
        }

    return@with Triple(photoUri, launcher, cameraPermission)
}
