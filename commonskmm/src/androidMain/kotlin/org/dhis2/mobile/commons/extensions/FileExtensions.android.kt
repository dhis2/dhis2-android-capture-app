package org.dhis2.mobile.commons.extensions

import java.io.File

actual fun getFormattedFileSize(filePath: String): String {
    return File(filePath).takeIf { it.exists() }?.length().let {
        fileSizeLabel(it ?: 0)
    }
}
