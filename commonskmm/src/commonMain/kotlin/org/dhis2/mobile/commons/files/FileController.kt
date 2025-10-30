package org.dhis2.mobile.commons.files

import java.io.File

interface FileController {
    fun resize(
        path: String,
        dimension: FileDimension = FileDimension.MEDIUM,
    ): File
}

enum class FileDimension {
    SMALL,
    MEDIUM,
    LARGE,
}
