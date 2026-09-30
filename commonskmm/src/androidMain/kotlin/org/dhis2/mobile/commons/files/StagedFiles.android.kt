package org.dhis2.mobile.commons.files

import android.content.Context
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss"
private const val IMAGE_CAPTURE_PREFIX = "IMG_"
private const val SIGNATURE_PREFIX = "SIGN_"
private const val JPEG_SUFFIX = ".jpg"
private const val PNG_SUFFIX = ".png"

fun createImageCaptureFile(context: Context): File = createStagedFile(context, IMAGE_CAPTURE_PREFIX, JPEG_SUFFIX)

fun createSignatureFile(context: Context): File = createStagedFile(context, SIGNATURE_PREFIX, PNG_SUFFIX)

fun deleteStagedFile(path: String) {
    val file = File(path)
    if (file.exists() && !file.delete()) {
        Timber.w("Could not delete the staged file %s", file.name)
    }
}

@Throws(IOException::class)
private fun createStagedFile(
    context: Context,
    prefix: String,
    suffix: String,
): File {
    val timestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(Date())
    return File.createTempFile(
        "$prefix${timestamp}_",
        suffix,
        FileResourceDirectoryHelper.getFileCacheResourceDirectory(context),
    )
}
