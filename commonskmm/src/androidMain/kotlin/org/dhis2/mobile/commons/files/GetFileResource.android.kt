package org.dhis2.mobile.commons.files

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContract
import java.io.File

actual class GetFileResource : ActivityResultContract<String, List<Uri>>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent.takeIf {
            resultCode == Activity.RESULT_OK
        }?.getClipDataUris() ?: emptyList()
    }
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

fun Uri.toFile(context: Context, suffix: String = ""): File? {
    var resultFile: File? = null
    if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        val cr = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extensionsFile = mimeTypeMap.getExtensionFromMimeType(cr.getType(this))
        resultFile = File.createTempFile(
            "tempFile",
            "$suffix.$extensionsFile",
            context.cacheDir,
        )
        val input = cr.openInputStream(this)
        resultFile.outputStream().use { stream ->
            input?.copyTo(stream)
        }
        input?.close()
    }
    return resultFile
}
