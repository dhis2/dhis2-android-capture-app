package org.dhis2.commons.data

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.dhis2.mobile.commons.files.FileHandlerImpl
import java.io.File
import java.io.FileOutputStream

class FileHandler {
    private val fileHandler = FileHandlerImpl()
    private val destinationResult = MutableLiveData<File>()

    fun saveBitmapAndOpen(
        bitmap: Bitmap,
        outputFileName: String,
        fileCallback: (LiveData<File>) -> Unit,
    ) {
        fileCallback(destinationResult)

        val imagesFolder = fileHandler.getDownloadDirectory(outputFileName)
        destinationResult.value = saveBitmapAndOpen(bitmap, imagesFolder)
    }

    private fun saveBitmapAndOpen(
        bitmap: Bitmap,
        destinationFolder: File,
    ): File {
        val os = FileOutputStream(destinationFolder)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.close()
        return destinationFolder
    }
}
