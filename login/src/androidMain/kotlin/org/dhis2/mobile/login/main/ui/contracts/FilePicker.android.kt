package org.dhis2.mobile.login.main.ui.contracts

import android.annotation.SuppressLint
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@SuppressLint("Recycle")
@Composable
actual fun filePicker(onResult: (String?) -> Unit): FilePicker {
    val contentResolver = LocalContext.current.contentResolver

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                val fileType =
                    with(contentResolver) {
                        MimeTypeMap.getSingleton().getExtensionFromMimeType(getType(uri))
                    }
                val suffix = if (fileType != null && fileType.isNotBlank()) ".$fileType" else null
                val file = File.createTempFile("importedDb", suffix)
                val inputStream = contentResolver.openInputStream(uri)!!
                try {
                    FileOutputStream(file, false).use { outputStream ->
                        var read: Int
                        val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (inputStream.read(bytes).also { read = it } != -1) {
                            outputStream.write(bytes, 0, read)
                        }
                    }
                } catch (e: IOException) {
                    Timber.e("Failed to load file: %s", e.message.toString())
                }
                onResult(file.path)
            } ?: onResult(null)
        }

    return object : FilePicker {
        override fun launch() {
            launcher.launch("*/*")
        }
    }
}
