package org.dhis2.mobile.commons.extensions

fun fileSizeLabel(fileSize: Long): String {
    val kb = fileSize / 1024f
    val mb = kb / 1024f
    return if (kb < 1024f) {
        "%.0fKB".format(kb)
    } else {
        "%.2fMB".format(mb)
    }
}
