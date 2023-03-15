package org.dhis2.data.service

import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface SyncRepository {
    fun getTeiByNotInStates(uid: String, states: List<State>): List<TrackedEntityInstance>
    fun getTeiByInStates(uid: String, states: List<State>): List<TrackedEntityInstance>
    fun getEventsFromEnrollmentByNotInSyncState(uid: String, states: List<State>): List<Event>
}
