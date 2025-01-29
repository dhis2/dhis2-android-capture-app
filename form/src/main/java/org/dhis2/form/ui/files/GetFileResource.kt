package org.dhis2.form.ui.files

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.dhis2.commons.bindings.rotateImage
import org.dhis2.commons.data.FormFileProvider
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import java.io.File

private class GetFileResource(
    private val allowMultipleSelection: Boolean = false,
) : ActivityResultContract<String, List<Uri>>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleSelection)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent.takeIf {
            resultCode == Activity.RESULT_OK
        }?.getClipDataUris() ?: emptyList()
    }
}

@Composable
fun rememberFilePicker(
    onResult: (String) -> Unit,
) = with(LocalContext.current) {
    val launcher = rememberLauncherForActivityResult(
        contract = GetFileResource(),
        onResult = { uris ->
            uris.firstOrNull()?.toFile(context = this)?.path?.let(onResult)
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
    val tempFile = File(
        FileResourceDirectoryHelper.getFileResourceDirectory(this),
        "tempFile.png",
    )

    val photoUri = FileProvider.getUriForFile(
        this,
        FormFileProvider.fileProviderAuthority,
        tempFile,
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onSuccess(tempFile.rotateImage(this).path)
            } else {
                onError()
            }
        },
    )
    val cameraPermission = rememberLauncherForActivityResult(
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

internal fun Uri.toFile(context: Context, suffix: String = ""): File? {
    var resultFile: File? = null
    if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        val cr = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extensionsFile = mimeTypeMap.getExtensionFromMimeType(cr.getType(this))
        resultFile = File.createTempFile(
            "tempFile",
            "$suffix.$extensionsFile",
            context.cacheDir,
        )
        val input = cr.openInputStream(this)
        resultFile.outputStream().use { stream ->
            input?.copyTo(stream)
        }
        input?.close()
    }
    return resultFile
}

internal fun Intent.getClipDataUris(): List<Uri> {
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return emptyList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}
