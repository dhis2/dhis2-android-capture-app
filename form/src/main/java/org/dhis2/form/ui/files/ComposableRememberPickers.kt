package org.dhis2.form.ui.files

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.dhis2.commons.data.FormFileProvider
import org.dhis2.mobile.commons.files.GetFileResource
import org.dhis2.mobile.commons.files.createImageCaptureFile
import org.dhis2.mobile.commons.files.toFileOverWrite
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

/**
 * Drives the camera capture of an image field: asks for the permission when it is missing, and hands
 * back the path of the photo that was taken.
 */
class CameraPicker internal constructor(
    private val onTakePicture: () -> Unit,
    private val onRequestPermission: () -> Unit,
) {
    fun takePicture() = onTakePicture()

    fun requestCameraPermission() = onRequestPermission()
}

@Composable
fun rememberCameraPicker(
    onSuccess: (String) -> Unit,
    onError: () -> Unit,
    onPermissionAccepted: () -> Unit,
): CameraPicker {
    val context = LocalContext.current
    val pendingCapture = remember { mutableStateOf<File?>(null) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            val captureFile = pendingCapture.value
            pendingCapture.value = null
            if (success && captureFile != null) {
                onSuccess(captureFile.path)
            } else {
                onError()
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { accepted ->
            if (accepted) {
                onPermissionAccepted()
                takePictureLauncher.launch(context.newCaptureUri(pendingCapture))
            } else {
                onError()
            }
        }

    return remember(takePictureLauncher, cameraPermissionLauncher) {
        CameraPicker(
            onTakePicture = { takePictureLauncher.launch(context.newCaptureUri(pendingCapture)) },
            onRequestPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
        )
    }
}

/**
 * Creates the destination of a single capture. It is one file per capture and not one per field,
 * because the Sdk names the file resource after the file it is handed: a name shared by two fields,
 * or by two captures, would put both of them in the same file and send the wrong name to the server.
 */
private fun Context.newCaptureUri(pendingCapture: MutableState<File?>): Uri {
    val captureFile = createImageCaptureFile(this)
    pendingCapture.value = captureFile
    return FileProvider.getUriForFile(
        this,
        FormFileProvider.fileProviderAuthority,
        captureFile,
    )
}
