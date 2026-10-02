package org.dhis2.mobile.commons.files

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException

class FileHandlerImpl(
    context: Context,
) : FileHandler {
    private val contentResolver = context.applicationContext.contentResolver

    override suspend fun copyAndOpen(
        sourceFile: File,
        fileCallback: () -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage: public Downloads can only be written through MediaStore
            copyToDownloads(sourceFile)
        } else {
            copyFile(sourceFile, getDownloadDirectory(sourceFile.name))
        }
        fileCallback()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun copyToDownloads(sourceFile: File) {
        val values =
            ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.Downloads.MIME_TYPE, sourceFile.mimeType())
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                // Keeps the entry hidden from other apps until it is fully written
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        val uri =
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Could not create ${sourceFile.name} in Downloads")
        try {
            val outputStream =
                contentResolver.openOutputStream(uri)
                    ?: throw IOException("Could not open ${sourceFile.name} in Downloads")
            outputStream.use { output ->
                sourceFile.inputStream().use { input -> input.copyTo(output) }
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, values, null, null)
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            throw e
        }
    }

    private fun copyFile(
        sourceFile: File,
        destinationDirectory: File,
    ): File = sourceFile.copyTo(destinationDirectory, true)

    private fun getDownloadDirectory(outputFileName: String): File =
        File
            .createTempFile(
                "copied_",
                outputFileName,
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                ),
            ).also {
                if (it.exists()) it.delete()
            }

    private fun File.mimeType(): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            ?: "application/octet-stream"
}
