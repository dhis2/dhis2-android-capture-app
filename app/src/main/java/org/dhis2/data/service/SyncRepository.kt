package org.dhis2.data.service

import io.reactivex.Observable
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress

interface SyncRepository {
    fun getTeiByNotInStates(
        uid: String,
        states: List<State>,
    ): List<TrackedEntityInstance>

    fun getTeiByInStates(
        uid: String,
        states: List<State>,
    ): List<TrackedEntityInstance>

    fun getEventsFromEnrollmentByNotInSyncState(
        uid: String,
        states: List<State>,
    ): List<Event>

    fun uploadEvent(eventUid: String): Observable<D2Progress>

    fun downLoadEvent(eventUid: String): Observable<out D2Progress>

    fun downloadEventFiles(eventUid: String): Observable<D2Progress>

    fun uploadTrackerProgram(programUid: String): Observable<D2Progress>

    fun downloadTrackerProgram(programUid: String): Observable<TrackerD2Progress>

    fun uploadEventProgram(programUid: String): Observable<D2Progress>

    fun downloadEventProgram(programUid: String): Observable<TrackerD2Progress>

    fun downloadProgramFiles(programUid: String): Observable<D2Progress>

    fun uploadTei(
        teiUid: String,
        programUid: String?,
    ): Observable<D2Progress>

    fun downloadTei(
        teiUid: String,
        programUid: String?,
    ): Observable<TrackerD2Progress>

    fun downloadTeiFiles(
        teiUid: String,
        programUid: String?,
    ): Observable<D2Progress>
}
