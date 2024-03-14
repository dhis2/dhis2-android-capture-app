package org.dhis2.form.data

import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import java.io.File

class FileController {
    fun resize(
        path: String,
        dimension: FileResizerHelper.Dimension = FileResizerHelper.Dimension.MEDIUM,
    ) = FileResizerHelper.resizeFile(File(path), dimension)
}
