package org.dhis2.usescases.main.domain.model

sealed interface DownloadMethod {
    data class Url(
        val url: String,
    ) : DownloadMethod

    data class File(
        val path: String,
    ) : DownloadMethod
}
