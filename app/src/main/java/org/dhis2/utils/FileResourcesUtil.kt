package org.dhis2.utils

import android.content.Context
import androidx.work.*
import org.dhis2.data.service.files.FilesWorker
import timber.log.Timber
import java.io.*

class FileResourcesUtil {


    companion object FileResourcesUtil {

        fun getUploadDirectory(context: Context): File {
            return File(context.filesDir, "upload")
        }

        fun getDownloadDirectory(context: Context): File {
            return File(context.filesDir, "images")
        }

        fun initFileUploadWork(teiUid: String, attrUid: String) {
            val fileBuilder = OneTimeWorkRequest.Builder(FilesWorker::class.java)
            fileBuilder.addTag("$teiUid.$attrUid")
            fileBuilder.setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            fileBuilder.setInputData(Data.Builder()
                    .putString(FilesWorker.MODE, FilesWorker.FileMode.UPLOAD.name)
                    .putString(FilesWorker.TEIUID, teiUid)
                    .putString(FilesWorker.ATTRUID, attrUid)
                    .build())
            val requestFile = fileBuilder.build()
            WorkManager.getInstance().beginUniqueWork("$teiUid.$attrUid", ExistingWorkPolicy.REPLACE, requestFile).enqueue()
        }


        fun initBulkFileUploadWork() {
            val fileBuilder = OneTimeWorkRequest.Builder(FilesWorker::class.java)
            fileBuilder.addTag(FilesWorker.TAG_UPLOAD)
            fileBuilder.setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            fileBuilder.setInputData(Data.Builder()
                    .putString(FilesWorker.MODE, FilesWorker.FileMode.UPLOAD.name)
                    .build())
            val requestFile = fileBuilder.build()
            WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG_UPLOAD, ExistingWorkPolicy.REPLACE, requestFile).enqueue()
        }

        fun initDownloadWork() {
            val fileBuilder = OneTimeWorkRequest.Builder(FilesWorker::class.java)
            fileBuilder.addTag(FilesWorker.TAG)
            fileBuilder.setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            fileBuilder.setInputData(Data.Builder()
                    .putString(FilesWorker.MODE, FilesWorker.FileMode.DOWNLOAD.name)
                    .build())
            val requestFile = fileBuilder.build()
            WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG, ExistingWorkPolicy.REPLACE, requestFile).enqueue()
        }

        fun saveImageToUpload(context: Context, file: File): Boolean {
            try {
                val futureUploadImage = File(context.filesDir.toString() + File.separator + "upload" + File.separator + file.name)

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(4096)
                    val fileSize = file.length()
                    var fileSizeDownloaded: Long = 0

                    inputStream = FileInputStream(file)
                    outputStream = FileOutputStream(futureUploadImage)

                    while (true) {
                        val read = inputStream.read(fileReader)

                        if (read == -1) {
                            break
                        }

                        outputStream.write(fileReader, 0, read)

                        fileSizeDownloaded += read.toLong()

                        Timber.d("file download: $fileSizeDownloaded of $fileSize")
                    }

                    outputStream.flush()

                    return true
                } catch (e: IOException) {
                    return false
                } finally {
                    inputStream?.close()

                    outputStream?.close()
                }
            } catch (e: IOException) {
                return false
            }

        }
    }
}