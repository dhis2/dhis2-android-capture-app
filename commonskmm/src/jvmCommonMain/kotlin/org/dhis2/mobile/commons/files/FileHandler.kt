package org.dhis2.mobile.commons.files

import java.io.File

fun interface FileHandler {
    suspend fun copyAndOpen(
        sourceFile: File,
        fileCallback: () -> Unit,
    )
}
