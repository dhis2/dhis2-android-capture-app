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
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                try {
                    val fileType =
                        with(contentResolver) {
                            MimeTypeMap.getSingleton().getExtensionFromMimeType(getType(uri))
                        }
                    val suffix = getFileSuffix(fileType)
                    val file = File.createTempFile("importedDb", suffix, context.cacheDir)

                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        inputStream.use { ins ->
                            FileOutputStream(file, false).use { outputStream ->
                                var read: Int
                                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                                while (ins.read(bytes).also { read = it } != -1) {
                                    outputStream.write(bytes, 0, read)
                                }
                            }
                        }
                        onResult(file.absolutePath)
                    } else {
                        onResult(null)
                    }
                } catch (e: IOException) {
                    Timber.e("Failed to load file: %s", e.message.toString())
                    onResult(null)
                }
            } ?: onResult(null)
        }

    return FilePicker {
        launcher.launch("*/*")
    }
}

fun getFileSuffix(fileType: String?): String? = if (fileType != null && fileType.isNotBlank()) ".$fileType" else null
