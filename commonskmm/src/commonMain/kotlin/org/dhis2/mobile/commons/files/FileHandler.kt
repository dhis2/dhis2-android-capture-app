package org.dhis2.mobile.commons.files

import java.io.File

interface FileHandler {
    fun copyAndOpen(
        sourceFile: File,
        fileCallback: () -> Unit,
    )
}
