package org.dhis2.mobile.commons.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Upper bound for the largest side of the decoded bitmap. Images are stored close to the resolution
 * they were captured at, so decoding one at full size would allocate tens of megabytes for a preview
 * that is never displayed larger than the screen.
 */
private const val MAX_PREVIEW_DIMENSION_PX = 2048

private const val SAMPLE_SIZE_STEP = 2
private const val ROTATION_90 = 90F
private const val ROTATION_180 = 180F
private const val ROTATION_270 = 270F
private const val MIRROR = -1F
private const val KEEP = 1F

/**
 * Decodes the image at this path for display, subsampled so that it fits in memory and with the Exif
 * orientation baked in.
 *
 * The orientation has to be applied here because the decoder ignores it: an image that is only
 * upright thanks to its Exif tag would be rendered sideways. Images the Sdk has re-encoded carry no
 * tag, since encoding a bitmap drops the metadata, and are left untouched.
 */
actual fun String.toImageBitmap(): ImageBitmap? {
    val file = File(this).takeIf { it.exists() } ?: return null
    val bounds = decodeBounds(file) ?: return null

    val bitmap =
        try {
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSizeFor(bounds, MAX_PREVIEW_DIMENSION_PX)
                }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: OutOfMemoryError) {
            Timber.w(e, "Not enough memory to decode %s", file.name)
            null
        } ?: return null

    return bitmap.applyExifOrientation(file).asImageBitmap()
}

/**
 * Reads the header of the image, which gives the dimensions needed to bound the memory the working
 * bitmap takes. Returns null when the file is not an image this platform can decode.
 */
private fun decodeBounds(file: File): BitmapFactory.Options? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, options)
    return options.takeIf { it.outWidth > 0 && it.outHeight > 0 }
}

/**
 * Smallest power of two that brings the decoded largest side within [maxDimensionPx]. The bound is
 * never exceeded, because it is what keeps the allocation predictable, and never upsampled either:
 * images already smaller than it are decoded as they are.
 */
private fun sampleSizeFor(
    bounds: BitmapFactory.Options,
    maxDimensionPx: Int,
): Int {
    var sampleSize = 1
    val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
    while (largestSide / sampleSize > maxDimensionPx) {
        sampleSize *= SAMPLE_SIZE_STEP
    }
    return sampleSize
}

private fun Bitmap.applyExifOrientation(file: File): Bitmap {
    val matrix = exifMatrix(file) ?: return this
    val oriented = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (oriented !== this) {
        recycle()
    }
    return oriented
}

/**
 * The transformation the Exif orientation asks for, or null when the image is already upright. The
 * mirrored orientations are covered as well: they are rare, but the decoder ignores them just the
 * same.
 */
private fun exifMatrix(file: File): Matrix? {
    val orientation =
        try {
            ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: IOException) {
            Timber.w(e, "Could not read the orientation of %s", file.name)
            return null
        }

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrixOf(rotation = ROTATION_90)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrixOf(rotation = ROTATION_180)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrixOf(rotation = ROTATION_270)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrixOf(scaleX = MIRROR)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrixOf(scaleY = MIRROR)
        ExifInterface.ORIENTATION_TRANSPOSE -> matrixOf(rotation = ROTATION_90, scaleX = MIRROR)
        ExifInterface.ORIENTATION_TRANSVERSE -> matrixOf(rotation = ROTATION_270, scaleX = MIRROR)
        else -> null
    }
}

private fun matrixOf(
    rotation: Float = 0F,
    scaleX: Float = KEEP,
    scaleY: Float = KEEP,
) = Matrix().apply {
    postRotate(rotation)
    postScale(scaleX, scaleY)
}
