package org.dhis2.metadata.usecases

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceDownloader

class TrackedEntityInstanceConfiguration(private val d2: D2) {

    fun downloader(teiUid: String, programUid: String?): TrackedEntityInstanceDownloader {
        return if (programUid != null) {
            d2.trackedEntityModule().trackedEntityInstanceDownloader()
                .byUid().eq(teiUid)
                .byProgramUid(programUid)
        } else {
            d2.trackedEntityModule().trackedEntityInstanceDownloader()
                .byUid().eq(teiUid)
        }
    }

    fun downloadWithReason(
        downloader: TrackedEntityInstanceDownloader,
        teiUid: String,
        programUid: String,
        reason: String,
    ) {
        setDownloadReason(teiUid, programUid, reason)
        downloadAndOverwrite(downloader)
    }

    private fun setDownloadReason(teiUid: String, programUid: String, reason: String) {
        d2.trackedEntityModule().ownershipManager()
            .blockingBreakGlass(teiUid, programUid, reason)
    }

    fun downloadAndOverwrite(downloader: TrackedEntityInstanceDownloader) {
        downloader.overwrite(true).blockingDownload()
    }

    fun hasBeenDownloaded(teiUid: String): Boolean {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingExists()
    }

    fun hasEnrollmentInProgram(teiUid: String, programUid: String): Boolean {
        return !d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(programUid)
            .blockingIsEmpty()
    }

    fun enrollmentUid(teiUid: String, programUid: String): String? {
        return d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(programUid)
            .one()
            .blockingGet()
            ?.uid()
    }
}
