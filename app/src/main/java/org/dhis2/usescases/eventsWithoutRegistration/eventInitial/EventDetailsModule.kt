package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.hisp.dhis.android.core.D2

@Module
class EventDetailsModule(val eventUid: String) {

    @Provides
    @PerFragment
    fun eventDetailsViewModelFactory(d2: D2): EventDetailsViewModelFactory {
        return EventDetailsViewModelFactory(eventUid, d2)
    }
}