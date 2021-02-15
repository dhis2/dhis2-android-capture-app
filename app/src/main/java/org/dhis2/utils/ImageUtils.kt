package org.dhis2.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper

class ImageUtils {

    fun rotateImage(context: Context, file: File): File {
        val ei = ExifInterface(file.path)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var bitmap = BitmapFactory.decodeFile(
            file.path, BitmapFactory.Options().apply { inSampleSize = 4 }
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
}
