package org.dhis2.mobile.commons.files

import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper
import java.io.File

class FileControllerImpl : FileController {
    override fun resize(
        path: String,
        dimension: FileDimension,
    ): File {
        val fileHelperDimension =
            when (dimension) {
                FileDimension.SMALL -> {
                    FileResizerHelper.Dimension.SMALL
                }
                FileDimension.MEDIUM -> {
                    FileResizerHelper.Dimension.MEDIUM
                }
                FileDimension.LARGE -> {
                    FileResizerHelper.Dimension.LARGE
                }
            }

        return FileResizerHelper.resizeFile(File(path), fileHelperDimension)
    }
}
