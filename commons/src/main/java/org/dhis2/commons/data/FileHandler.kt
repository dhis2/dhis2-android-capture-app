package org.dhis2.commons.data

import android.Manifest
import android.graphics.Bitmap
import android.os.Environment
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FileHandler @Inject constructor(
    registry: ActivityResultRegistry,
) {

    private val destinationResult = MutableLiveData<File>()
    private val permissionResult = MutableLiveData<Boolean>()

    private val requestStoragePermission =
        registry.register(
            REGISTRY_FILE_PERMISSION,
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            val granted = it.values.all { isGranted -> isGranted }
            permissionResult.value = granted
        }

    fun saveBitmap(
        bitmap: Bitmap,
        outputFileName: String,
        permissionsCallback: (LiveData<Boolean>) -> Unit,
        fileCallback: (LiveData<File>) -> Unit,
    ) {
        permissionsCallback(permissionResult)
        fileCallback(destinationResult)

        val imagesFolder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS,
            ),
            "dhis2" + File.separator + outputFileName,
        )

        val os = FileOutputStream(imagesFolder)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.close()

        destinationResult.value = imagesFolder
    }

    fun copyAndOpen(
        sourceFile: File,
        permissionsCallback: (LiveData<Boolean>) -> Unit,
        fileCallback: (LiveData<File>) -> Unit,
    ) {
        permissionsCallback(permissionResult)
        fileCallback(destinationResult)

        val imagesFolder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS,
            ),
            "dhis2" + File.separator + sourceFile.name,
        )
        destinationResult.value = copyFile(sourceFile, imagesFolder)
    }

    private fun copyFile(sourceFile: File, destinationDirectory: File): File {
        requestStoragePermission.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ),
        )

        return sourceFile.copyTo(destinationDirectory, true)
    }

    companion object {
        private const val REGISTRY_FILE_PERMISSION = "FILE PERMISSIONS"
    }
}
