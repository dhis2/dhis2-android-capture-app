package org.dhis2.mobile.commons.files

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContract
import java.io.File

actual class GetFileResource : ActivityResultContract<String, List<Uri>>() {
    override fun createIntent(
        context: Context,
        input: String,
    ): Intent =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): List<Uri> =
        intent
            .takeIf {
                resultCode == Activity.RESULT_OK
            }?.getClipDataUris() ?: emptyList()
}

internal fun Intent.getClipDataUris(): List<Uri> {
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return emptyList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}

fun Uri.toFileOverWrite(
    context: Context,
    suffix: String = "",
): File? {
    var resultFile: File? = null
    if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        val cr = context.contentResolver

        val displayName =
            cr.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }

        val fileName = displayName?.substringBeforeLast('.') ?: STAGED_FILE_FALLBACK_NAME

        // The extension the provider reports for the content is more reliable than the one in the
        // display name, but the display name is all there is when the mime type is not recognised.
        val extension =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(this))
                ?: displayName?.substringAfterLast('.', "")

        val fullName = "$fileName$suffix.${extension.orEmpty()}".trimEnd('.')
        resultFile = File(context.cacheDir, fullName)
        val input = cr.openInputStream(this)
        resultFile.outputStream().use { stream ->
            input?.copyTo(stream)
        }
        input?.close()
    }
    return resultFile
}

private const val STAGED_FILE_FALLBACK_NAME = "attachment"
