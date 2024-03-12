package org.dhis2.usescases.teiDownload

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.metadata.usecases.FileResourceConfiguration
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityInstanceConfiguration
import org.dhis2.usescases.searchTrackEntity.TeiDownloadResult
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceDownloader

class TeiDownloader(
    private val programConfiguration: ProgramConfiguration,
    private val teiConfiguration: TrackedEntityInstanceConfiguration,
    private val fileConfiguration: FileResourceConfiguration,
    private val currentProgram: String?,
    private val resources: ResourceManager,
) {

    private var downloadRepository: TrackedEntityInstanceDownloader? = null

    fun download(teiUid: String, enrollmentUid: String?, reason: String?): TeiDownloadResult {
        return if (isReadyForBreakTheGlass(reason)) {
            breakTheGlass(teiUid, reason!!)
        } else {
            defaultDownload(teiUid, enrollmentUid)
        }
    }

    private fun isReadyForBreakTheGlass(reason: String?): Boolean {
        return downloadRepository != null && reason != null
    }

    private fun defaultDownload(teiUid: String, enrollmentUid: String?): TeiDownloadResult {
        downloadRepository = teiConfiguration.downloader(teiUid, currentProgram)

        return try {
            teiConfiguration.downloadAndOverwrite(downloadRepository!!)
            checkDownload(teiUid, enrollmentUid)
        } catch (e: Exception) {
            when {
                e is D2Error -> handleD2Error(e, teiUid, enrollmentUid)
                e.cause is D2Error -> handleD2Error(e.cause as D2Error?, teiUid, enrollmentUid)
                else -> TeiDownloadResult.ErrorResult(e.localizedMessage ?: "")
            }
        }
    }

    private fun handleD2Error(
        d2Error: D2Error?,
        teiUid: String,
        enrollmentUid: String?,
    ): TeiDownloadResult {
        return when (d2Error!!.errorCode()) {
            D2ErrorCode.OWNERSHIP_ACCESS_DENIED -> TeiDownloadResult.BreakTheGlassResult(
                teiUid,
                enrollmentUid,
            )
            else -> TeiDownloadResult.ErrorResult(resources.parseD2Error(d2Error) ?: "")
        }
    }

    private fun breakTheGlass(teiUid: String, reason: String): TeiDownloadResult {
        return if (downloadRepository != null) {
            teiConfiguration.downloadWithReason(
                downloadRepository!!,
                teiUid,
                currentProgram!!,
                reason,
            )
            fileConfiguration.download()
            downloadRepository = null
            checkDownload(teiUid, null)
        } else {
            TeiDownloadResult.TeiNotDownloaded(teiUid)
        }
    }

    private fun checkDownload(teiUid: String, enrollmentUid: String?): TeiDownloadResult {
        return if (teiConfiguration.hasBeenDownloaded(teiUid)) {
            when {
                hasEnrollmentInCurrentProgram(teiUid) ->
                    TeiDownloadResult.DownloadedResult(
                        teiUid = teiUid,
                        programUid = currentProgram,
                        enrollmentUid = enrollmentUid ?: teiConfiguration.enrollmentUid(
                            teiUid,
                            currentProgram!!,
                        ),
                    )
                canEnrollInCurrentProgram() ->
                    TeiDownloadResult.TeiToEnroll(teiUid)
                else ->
                    TeiDownloadResult.DownloadedResult(
                        teiUid = teiUid,
                        programUid = currentProgram,
                        enrollmentUid = null,
                    )
            }
        } else {
            TeiDownloadResult.TeiNotDownloaded(teiUid)
        }
    }

    private fun hasEnrollmentInCurrentProgram(teiUid: String): Boolean {
        if (currentProgram == null) return false
        return teiConfiguration.hasEnrollmentInProgram(teiUid, currentProgram)
    }

    private fun canEnrollInCurrentProgram(): Boolean {
        if (currentProgram == null) return false
        return programConfiguration.canEnrollNewTei(currentProgram)
    }
}
