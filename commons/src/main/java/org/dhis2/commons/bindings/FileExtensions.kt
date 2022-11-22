package org.dhis2.commons.bindings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import java.io.File
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
        this.path, BitmapFactory.Options().apply { inSampleSize = 4 }
    )

    bitmap = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)
        else -> bitmap
    }

    return File(
        FileResourceDirectoryHelper.getFileResourceDirectory(context), "tempFile.png"
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
