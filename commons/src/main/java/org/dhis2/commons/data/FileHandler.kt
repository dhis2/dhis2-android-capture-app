package org.dhis2.commons.data

import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream

class FileHandler {

    private val destinationResult = MutableLiveData<File>()

    fun saveBitmapAndOpen(
        bitmap: Bitmap,
        outputFileName: String,
        fileCallback: (LiveData<File>) -> Unit,
    ) {
        fileCallback(destinationResult)

        val imagesFolder = getDownloadDirectory(outputFileName)
        destinationResult.value = saveBitmapAndOpen(bitmap, imagesFolder)
    }

    fun copyAndOpen(
        sourceFile: File,
        fileCallback: (LiveData<File>) -> Unit,
    ) {
        fileCallback(destinationResult)

        val imagesFolder = getDownloadDirectory(sourceFile.name)
        destinationResult.value = copyFile(sourceFile, imagesFolder)
    }

    private fun saveBitmapAndOpen(bitmap: Bitmap, destinationFolder: File): File {
        val os = FileOutputStream(destinationFolder)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.close()
        return destinationFolder
    }

    private fun copyFile(sourceFile: File, destinationDirectory: File): File {
        return sourceFile.copyTo(destinationDirectory, true)
    }

    private fun getDownloadDirectory(outputFileName: String) = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        ),
        "dhis2" + File.separator + outputFileName,
    )
}
