package org.dhis2.commons.data

import android.content.Context
import android.graphics.Bitmap
import org.dhis2.mobile.commons.files.FileHandlerImpl
import java.io.File
import java.io.FileOutputStream

class FileHandler(
    context: Context,
) {
    private val cacheDir = context.cacheDir
    private val fileHandler = FileHandlerImpl(context)

    /**
     * Saves [bitmap] as a PNG in the device Downloads folder and invokes [onSaved] on the
     * caller's dispatcher. Throws if the file cannot be written.
     */
    suspend fun saveBitmapAndOpen(
        bitmap: Bitmap,
        outputFileName: String,
        onSaved: () -> Unit,
    ) {
        // Written to the app cache first, then copied to Downloads (MediaStore on API 29+)
        val bitmapFile =
            saveBitmap(bitmap, File(cacheDir, outputFileName))
        fileHandler.copyAndOpen(bitmapFile, onSaved)
    }

    private fun saveBitmap(
        bitmap: Bitmap,
        destinationFile: File,
    ): File {
        FileOutputStream(destinationFile).use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        return destinationFile
    }
}
