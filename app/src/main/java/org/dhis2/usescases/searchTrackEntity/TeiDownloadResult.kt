package org.dhis2.usescases.searchTrackEntity

sealed class TeiDownloadResult() {
    data class DownloadedResult(
        val teiUid: String,
        val enrollmentUid: String?
    ) : TeiDownloadResult()

    data class TeiToEnroll(
        val teiUid: String
    ) : TeiDownloadResult()

    data class TeiNotDownloaded(
        val teiUid: String
    ) : TeiDownloadResult()

    data class BreakTheGlassResult(
        val teiUid: String,
        val enrollmentUid: String?
    ) : TeiDownloadResult()

    data class ErrorResult(
        val errorMessage: String
    ) : TeiDownloadResult()

    fun handleResult(
        onOpenDashboard: (teiUid: String, enrollmentUid: String?) -> Unit,
        onBreakTheGlassResult: (teiUid: String, enrollmentUid: String?) -> Unit,
        onNotDownloaded: (teiUid: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        when (this) {
            is BreakTheGlassResult -> onBreakTheGlassResult(teiUid, enrollmentUid)
            is DownloadedResult -> onOpenDashboard(teiUid, enrollmentUid)
            is ErrorResult -> onError(errorMessage)
            is TeiNotDownloaded -> onNotDownloaded(teiUid)
            is TeiToEnroll -> {}
        }
    }
}
