package org.dhis2.commons.bindings

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import org.apache.commons.io.FileUtils
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper

fun File.widthAndHeight(minimum: Int? = null): Pair<Int, Int> {
    BitmapFactory.decodeFile(this.absolutePath).apply {
        return resizeToMinimum(minimum, width, height)
    }
}

fun resizeToMinimum(minimum: Int? = null, width: Int, height: Int): Pair<Int, Int> {
    val ratio = width.toFloat() / height.toFloat()

    return if (minimum != null && width > height && width < minimum) {
        Pair(minimum, (minimum.toFloat() / ratio).toInt())
    } else if (minimum != null && height >= width && height < minimum) {
        Pair((minimum.toFloat() * ratio).toInt(), minimum)
    } else {
        Pair(width, height)
    }
}

fun File.rotateImage(context: Context): File {
    val ei = ExifInterface(this.path)
    val orientation =
        ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    var bitmap = BitmapFactory.decodeFile(
        this.path,
        BitmapFactory.Options().apply { inSampleSize = 4 }
    )

    bitmap = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)
        else -> bitmap
    }

    return File(
        FileResourceDirectoryHelper.getFileResourceDirectory(context),
        "tempFile.png"
    ).apply { writeBitmap(bitmap, Bitmap.CompressFormat.JPEG, 100) }
}

private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

fun getFileFromGallery(context: Context, imageUri: Uri?): File? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = imageUri?.let { context.contentResolver.query(it, projection, null, null, null) }
        ?: return null
    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    val s = cursor.getString(column_index)
    cursor.close()
    return File(s)
}

fun getFileFrom(context: Context, fileUri: Uri): File? {
    val file = getFilePath(context, fileUri)?.let { File(it) }
    val tempFile = File(
        FileResourceDirectoryHelper.getFileResourceDirectory(context),
        file?.name ?: "temp"
    )
    context.contentResolver.openInputStream(fileUri)?.let { inputStream ->
        FileUtils.copyToFile(inputStream, tempFile)
    }
    return tempFile
}

private fun getFilePath(context: Context, uri: Uri): String? {
    var copy = uri
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    if (DocumentsContract.isDocumentUri(context, copy)) {
        when {
            isDownloadsDocument(copy) -> {
                val id = DocumentsContract.getDocumentId(copy)
                copy = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()
                )
            }
            isExternalStorageDocument(copy) -> {
                val id = DocumentsContract.getDocumentId(copy)
                val split = id.split(":").toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
            isMediaDocument(copy) -> {
                val id = DocumentsContract.getDocumentId(copy)
                val split = id.split(":").toTypedArray()
                when (split[0]) {
                    "image" -> {
                        copy = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        copy = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        copy = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }

                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
    }

    if ("content".equals(copy.scheme, true)) {
        if (isGooglePhotosUri(copy)) {
            return copy.lastPathSegment
        }

        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            cursor = context.contentResolver?.query(
                copy,
                projection,
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0)
            }

            return null
        } catch (ignore: Exception) {
        } finally {
            cursor?.close()
        }
    } else if ("file".equals(copy.scheme, true)) {
        return copy.path
    }

    return null
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}
