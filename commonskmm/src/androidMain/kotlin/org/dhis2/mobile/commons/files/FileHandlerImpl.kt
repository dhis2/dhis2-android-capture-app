package org.dhis2.mobile.commons.files

import android.os.Build
import android.os.Environment
import java.io.File

class FileHandlerImpl : FileHandler {
    override fun copyAndOpen(
        sourceFile: File,
        fileCallback: () -> Unit,
    ) {
        val folder = getDownloadDirectory(sourceFile.name)
        copyFile(sourceFile, folder)
        fileCallback()
    }

    private fun copyFile(
        sourceFile: File,
        destinationDirectory: File,
    ): File = sourceFile.copyTo(destinationDirectory, true)

    fun getDownloadDirectory(outputFileName: String): File =
        if (Build.VERSION.SDK_INT >= 29) {
            File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                ),
                "dhis2" + File.separator + outputFileName,
            )
        } else {
            File.createTempFile(
                "copied_",
                outputFileName,
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                ),
            )
        }.also {
            if (it.exists()) it.delete()
        }
}
