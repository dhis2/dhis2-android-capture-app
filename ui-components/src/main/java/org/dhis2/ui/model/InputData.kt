package org.dhis2.ui.model

import org.dhis2.ui.extensions.decimalFormat

sealed class InputData {
    data class FileInputData(
        val fileName: String,
        private val fileSize: Long,
        val filePath: String,
    ) {
        val fileSizeLabel
            get() = run {
                val kb = fileSize / 1024f
                val mb = kb / 1024f
                if (kb < 1024f) {
                    "${kb.decimalFormat("*0")}KB"
                } else {
                    "${mb.decimalFormat()}MB"
                }
            }
    }
}
