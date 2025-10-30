package org.dhis2.usescases.main.domain.model

import android.net.Uri

sealed interface DownloadMethod {
    data class Url(
        val uri: Uri,
    ) : DownloadMethod

    data class File(
        val uri: Uri,
    ) : DownloadMethod
}
