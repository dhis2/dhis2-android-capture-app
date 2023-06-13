package org.dhis2.form.data

import java.io.File
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper

class FileController {
    fun resize(
        path: String,
        dimension: FileResizerHelper.Dimension = FileResizerHelper.Dimension.MEDIUM
    ) = FileResizerHelper.resizeFile(File(path), dimension)
}
