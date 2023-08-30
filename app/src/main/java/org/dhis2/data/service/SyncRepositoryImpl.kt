package org.dhis2.data.service

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class SyncRepositoryImpl(private val d2: D2) : SyncRepository {

    override fun getTeiByNotInStates(
        uid: String,
        states: List<State>,
    ): List<TrackedEntityInstance> {
        return d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(uid)
            .byAggregatedSyncState().notIn(states)
            .blockingGet()
    }

    override fun getTeiByInStates(uid: String, states: List<State>): List<TrackedEntityInstance> {
        return d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(uid)
            .byAggregatedSyncState().`in`(states)
            .blockingGet()
    }

    override fun getEventsFromEnrollmentByNotInSyncState(
        uid: String,
        states: List<State>,
    ): List<Event> {
        return d2.eventModule().events()
            .byEnrollmentUid().eq(uid)
            .byAggregatedSyncState().notIn(State.SYNCED)
            .blockingGet()
    }
}
