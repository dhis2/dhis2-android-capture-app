package org.dhis2.mobile.commons.files

import android.os.Build
import android.os.Environment
import java.io.File

class FileHandlerImpl : FileHandler {
    override fun copyAndOpen(
        sourceFile: File,
        fileCallback: () -> Unit,
    ) {
        // On Android 10+ (API 29), scoped storage prevents direct file creation in Downloads
        // Keep the file in app-accessible storage; sharing should use FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Skip copy on modern Android - file is already accessible in app storage
            fileCallback()
        } else {
            // On older versions, we can still copy to Downloads
            val folder = getDownloadDirectory(sourceFile.name)
            copyFile(sourceFile, folder)
            fileCallback()
        }
    }

    private fun copyFile(
        sourceFile: File,
        destinationDirectory: File,
    ): File = sourceFile.copyTo(destinationDirectory, true)

    fun getDownloadDirectory(outputFileName: String): File =
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
}
