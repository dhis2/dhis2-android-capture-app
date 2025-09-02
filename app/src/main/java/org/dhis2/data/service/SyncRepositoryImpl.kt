package org.dhis2.data.service

import io.reactivex.Observable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class SyncRepositoryImpl(
    private val d2: D2,
) : SyncRepository {
    override fun getTeiByNotInStates(
        uid: String,
        states: List<State>,
    ): List<TrackedEntityInstance> =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .byUid()
            .eq(uid)
            .byAggregatedSyncState()
            .notIn(states)
            .blockingGet()

    override fun getTeiByInStates(
        uid: String,
        states: List<State>,
    ): List<TrackedEntityInstance> =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .byUid()
            .eq(uid)
            .byAggregatedSyncState()
            .`in`(states)
            .blockingGet()

    override fun getEventsFromEnrollmentByNotInSyncState(
        uid: String,
        states: List<State>,
    ): List<Event> =
        d2
            .eventModule()
            .events()
            .byEnrollmentUid()
            .eq(uid)
            .byAggregatedSyncState()
            .notIn(State.SYNCED)
            .blockingGet()

    override fun uploadEvent(eventUid: String) =
        d2
            .eventModule()
            .events()
            .byUid()
            .eq(eventUid)
            .upload()

    override fun downLoadEvent(eventUid: String): Observable<out D2Progress> =
        d2
            .eventModule()
            .eventDownloader()
            .byUid()
            .eq(eventUid)
            .download()

    override fun downloadEventFiles(eventUid: String) =
        d2
            .fileResourceModule()
            .fileResourceDownloader()
            .byEventUid()
            .eq(eventUid)
            .download()

    override fun uploadTrackerProgram(programUid: String) =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .byProgramUids(listOf(programUid))
            .upload()

    override fun downloadTrackerProgram(programUid: String) =
        d2
            .trackedEntityModule()
            .trackedEntityInstanceDownloader()
            .byProgramUid(programUid)
            .download()

    override fun uploadEventProgram(programUid: String) =
        d2
            .eventModule()
            .events()
            .byProgramUid()
            .eq(programUid)
            .upload()

    override fun downloadEventProgram(programUid: String) =
        d2
            .eventModule()
            .eventDownloader()
            .byProgramUid(programUid)
            .download()

    override fun downloadProgramFiles(programUid: String) =
        d2
            .fileResourceModule()
            .fileResourceDownloader()
            .byProgramUid()
            .eq(programUid)
            .download()

    override fun uploadTei(
        teiUid: String,
        programUid: String?,
    ) = d2
        .trackedEntityModule()
        .trackedEntityInstances()
        .byUid()
        .eq(teiUid)
        .byProgramUids(programUid?.let { listOf(it) } ?: emptyList())
        .upload()

    override fun downloadTei(
        teiUid: String,
        programUid: String?,
    ) = d2
        .trackedEntityModule()
        .trackedEntityInstanceDownloader()
        .byUid()
        .eq(teiUid)
        .byProgramUid(programUid ?: "")
        .download()

    override fun downloadTeiFiles(
        teiUid: String,
        programUid: String?,
    ) = d2
        .fileResourceModule()
        .fileResourceDownloader()
        .byTrackedEntityUid()
        .eq(teiUid)
        .byProgramUid()
        .eq(programUid ?: "")
        .download()
}
